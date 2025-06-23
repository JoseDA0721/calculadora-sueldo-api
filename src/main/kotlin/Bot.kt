package com.example

import com.example.dto.EmployeeResponse
import com.example.dto.EstimatedSalaryResponse
import com.example.dto.NewPaymentRequest
import com.example.dto.NewWorkLogRequest
import com.example.dto.PaymentRequestResponse
import com.example.dto.UpdateEmployeeRequest
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking

// Cliente HTTP para llamar a nuestra API de Ktor
val httpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

fun main() {
    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")
    val apiUrl = System.getenv("API_BASE_URL") ?: "http://localhost:8080"
    val periodRegex = Regex("^\\d{4}-(0[1-9]|1[0-2])$")

    if (botToken == null) {
        println("Error: La variable de entorno TELEGRAM_BOT_TOKEN no está definida.")
        return // Detiene la ejecución
    }

    println("Bot ejecutandose.")

    val bot =
        bot {
            token = botToken
            dispatch {
                command("start") {
                    val welcomeText =
                        """
                        👋 ¡Bienvenido al Asistente de Salarios!
                        
                        Para empezar, necesito saber quién eres. Por favor, regístrate usando tu número de cédula con el siguiente comando:
                        
                        `/registrar [tu_numero_de_cedula]`
                        
                        Por ejemplo: `/registrar 1722334455`
                        
                        Una vez registrado, podrás registrar horas, solicitar pagos y mucho más.
                        """.trimIndent()

                    bot.sendMessage(ChatId.fromId(message.chat.id), text = welcomeText)
                }
                command("registrar") {
                    val cedula = args.firstOrNull()
                    val chatId = message.chat.id

                    if (cedula == null) {
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "❌ Formato incorrecto. Debes incluir tu número de cédula. Ejemplo: `/registrar 1722334455`",
                        )
                        return@command
                    }

                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = "Procesando tu registro...",
                    )

                    runBlocking {
                        try {
                            val request = UpdateEmployeeRequest(telegramChatId = chatId)

                            val response =
                                httpClient.patch("$apiUrl/api/empleado/$cedula") {
                                    contentType(ContentType.Application.Json)
                                    setBody(request)
                                }

                            when (response.status) {
                                HttpStatusCode.OK -> {
                                    val successMessage =
                                        """
                                        ✅ ¡Registro exitoso! Tu chat ha sido vinculado a tu perfil.
                                        
                                        Estos son los comandos que puedes usar:
                                        
                                        • `/log [horas]` - Registra tus horas de trabajo del día.
                                          Ejemplo: `/log 8.5`
                                          
                                        • `/solicitar_pago [AAAA-MM]` - Envía una solicitud de pago para un período.
                                          Ejemplo: `/solicitar_pago 2025-06`
                                          
                                        • `/calcular_sueldo [AAAA-MM]` - Calcula tu sueldo aproximado para un período.
                                          Ejemplo: `/calcular_sueldo 2025-06`
                                          
                                        • `/estado_solicitud [AAAA-MM]` - Consulta el estado de una solicitud de pago.
                                          Ejemplo: `/estado_de_la_solicitud 2025-06`
                                          
                                        • `/mi_id` - Muestra tu ID de chat de Telegram.
                                        """.trimIndent()

                                    bot.sendMessage(
                                        ChatId.fromId(chatId),
                                        text = successMessage,
                                    )
                                }
                                HttpStatusCode.NotFound -> {
                                    bot.sendMessage(
                                        ChatId.fromId(chatId),
                                        text =
                                            "😕 No pudimos encontrarte. " +
                                                "Verifica que tu cédula (`$cedula`) sea correcta " +
                                                "y que estés registrado en el sistema.",
                                    )
                                }
                                else -> {
                                    bot.sendMessage(
                                        ChatId.fromId(chatId),
                                        text =
                                            "😕 Hubo un problema con tu registro. (" +
                                                "Código: ${response.status.value}). " +
                                                "Contacta al administrador.",
                                    )
                                    println("Error body: $response")
                                }
                            }
                        } catch (e: Exception) {
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text = "❌ Ocurrió un error de conexión al procesar tu registro.",
                            )
                            e.printStackTrace()
                        }
                    }
                }
                command("log") {
                    val text = args.joinToString(" ")
                    val hours = text.toDoubleOrNull()
                    val chatId = message.chat.id

                    if (hours == null) {
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "Formato incorrecto. Por favor, usa /log [número de horas]. Ejemplo: /log 7.5",
                        )
                        return@command
                    }

                    runBlocking {
                        try {
                            val employeeResponse = httpClient.get("$apiUrl/api/empleado/$chatId")
                            if (employeeResponse.status != HttpStatusCode.OK) {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Debe registrarse primero. Use /registrar [cédula].",
                                )
                                return@runBlocking
                            }

                            val employee = employeeResponse.body<EmployeeResponse>()

                            val request =
                                NewWorkLogRequest(
                                    employeeId = employee.id,
                                    hoursWorked = hours,
                                )

                            httpClient.post("$apiUrl/api/work-logs") {
                                contentType(ContentType.Application.Json)
                                setBody(request)
                            }
                            bot.sendMessage(
                                ChatId.fromId(message.chat.id),
                                text = "✅ Horas registradas con éxito: $hours horas para el empleado ID ${employee.id}.",
                            )
                        } catch (e: Exception) {
                            bot.sendMessage(
                                ChatId.fromId(message.chat.id),
                                text = "❌ Ocurrió un error al registrar las horas. Por favor, intenta más tarde.",
                            )
                            e.printStackTrace()
                        }
                    }
                }
                command("solicitar_pago") {
                    val period = args.joinToString(" ")
                    val chatId = message.chat.id

                    if (!period.matches(periodRegex)) {
                        println(period)
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "❌ Formato incorrecto. Por favor, usa /solicitar_pago AAAA-MM. Ejemplo: $period",
                        )
                        return@command
                    }

                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = "Procesando tu solicitud para el periodo $period...",
                    )

                    runBlocking {
                        try {
                            val employeeResponse = httpClient.get("$apiUrl/api/empleado/$chatId")
                            if (employeeResponse.status != HttpStatusCode.OK) {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Debe registrarse primero. Use /registrar [cédula].",
                                )
                                return@runBlocking
                            }
                            val employee = employeeResponse.body<EmployeeResponse>()

                            // b. Preparar la petición para la API
                            val request =
                                NewPaymentRequest(
                                    employeeId = employee.id,
                                    period = period,
                                )

                            // c. Llamar al endpoint para crear la solicitud
                            val response =
                                httpClient.post("$apiUrl/api/payment-request") {
                                    contentType(ContentType.Application.Json)
                                    setBody(request)
                                }

                            // d. Informar al usuario del resultado
                            if (response.status == HttpStatusCode.Created) {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text =
                                        """
                                        ✅ ¡Éxito! Tu solicitud de pago para el periodo $period 
                                        ha sido enviada y está pendiente de revisión.
                                        """.trimIndent(),
                                )
                            } else {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text =
                                        """
                                        😕 Hubo un problema al crear tu solicitud. 
                                        Por favor, inténtalo más tarde. 
                                        (Código: ${response.status.value})
                                        """.trimIndent(),
                                )
                            }
                        } catch (e: Exception) {
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text = "❌ Ocurrió un error de conexión al procesar tu solicitud.",
                            )
                            e.printStackTrace()
                        }
                    }
                }
                command("calcular_sueldo") {
                    val period = args.joinToString(" ")
                    val chatId = message.chat.id

                    if (!period.matches(periodRegex)) {
                        println(period)
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "❌ Formato incorrecto. Por favor, usa /solicitar_pago AAAA-MM. Ejemplo: $period",
                        )
                        return@command
                    }

                    runBlocking {
                        try {
                            val employeeResponse = httpClient.get("$apiUrl/api/empleado/$chatId")
                            if (employeeResponse.status != HttpStatusCode.OK) {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Debe registrarse primero. Use /registrar [cédula].",
                                )
                                return@runBlocking
                            }
                            val employee = employeeResponse.body<EmployeeResponse>()

                            val response =
                                httpClient.get("$apiUrl/api/payroll/calculate-salary") {
                                    url {
                                        parameters.append("employeeID", employee.id)
                                        parameters.append("period", period)
                                    }
                                }

                            if (response.status == HttpStatusCode.OK) {
                                val breakdown = response.body<EstimatedSalaryResponse>()

                                val formattedMessage =
                                    """
                                    💰 *Cálculo de Sueldo Estimado*
                                    
                                    Desglose para el período *${breakdown.period}*:
                                    
                                    - *Tarifa por Hora:* `${'$'}`*${"%.2f".format(breakdown.hourlyRate)}*
                                    - *Total de Horas Registradas:* *${breakdown.totalHours} horas*
                                    
                                    ---
                                    
                                    - *Sueldo Base (Tarifa × Horas):* `${'$'}`*${"%.2f".format(breakdown.baseSalary)}*
                                    - *Total de Descuentos:* `${'$'}`*${"%.2f".format(breakdown.totalDiscounts)}*
                                    
                                    ---
                                    
                                    - *Sueldo Neto Estimado:* **`${'$'}`${"%.2f".format(breakdown.estimatedNetSalary)}**
                                    """.trimIndent()
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = formattedMessage,
                                    parseMode = ParseMode.MARKDOWN,
                                )
                            } else {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "😕 No se pudo calcular tu sueldo. Asegúrate de tener horas registradas en ese período.",
                                )
                            }
                        } catch (e: Exception) {
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text = "❌ Ocurrió un error de conexión.",
                            )
                            e.printStackTrace()
                        }
                    }
                }
                command("estado_solicitud") {
                    val period = args.joinToString(" ")
                    val chatId = message.chat.id

                    if (!period.matches(periodRegex)) {
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "❌ Formato incorrecto. Ejemplo: /estado_de_la_solicitud 2025-07",
                        )
                        return@command
                    }

                    runBlocking {
                        try {
                            val employeeResponse = httpClient.get("$apiUrl/api/empleado/$chatId")
                            if (employeeResponse.status != HttpStatusCode.OK) {
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Debe registrarse primero. Use /registrar [cédula].",
                                )
                                return@runBlocking
                            }

                            val employee = employeeResponse.body<EmployeeResponse>()

                            val response =
                                httpClient.get("$apiUrl/api/payment-request/status") {
                                    url {
                                        parameters.append("employeeId", employee.id)
                                        parameters.append("period", period)
                                    }
                                }

                            if (response.status == HttpStatusCode.OK) {
                                val statusResponse = response.body<PaymentRequestResponse>()
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "📄 El estado de tu solicitud para $period es: **${statusResponse.status}**.",
                                )
                            } else {
                                bot.sendMessage(ChatId.fromId(chatId), text = "🤔 No encontré una solicitud para ese período.")
                            }
                        } catch (e: Exception) {
                            bot.sendMessage(ChatId.fromId(chatId), text = "❌ Ocurrió un error de conexión.")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    bot.startPolling()
}
