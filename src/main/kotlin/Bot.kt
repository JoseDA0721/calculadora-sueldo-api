package com.example

import com.example.dto.NewPaymentRequest
import com.example.dto.NewWorkLogRequest
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
// Leemos la configuración desde las Variables de Entorno
    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")
    val apiUrl = System.getenv("API_BASE_URL") ?: "http://localhost:8080"
    // En Bot.kt
    val periodRegex = Regex("^\\d{4}-(0[1-9]|1[0-2])$")

// Verificación de seguridad: si el token no está, el bot no puede arrancar.
    if (botToken == null) {
        println("Error: La variable de entorno TELEGRAM_BOT_TOKEN no está definida.")
        return // Detiene la ejecución
    }
    println("Iniciando bot...")

    val bot =
        bot {
            token = botToken

            dispatch {
                command("start") {
                    val welcomeText =
                        """
                        ¡Hola! Soy tu asistente de registro de horas.
                        Usa /log [horas] para registrar tu trabajo de hoy. Ejemplo: /log 8.5
                        """.trimIndent()

                    bot.sendMessage(ChatId.fromId(message.chat.id), text = welcomeText)
                }

                command("log") {
                    val text = args.joinToString(" ")
                    val hours = text.toDoubleOrNull()

                    if (hours == null) {
                        bot.sendMessage(
                            ChatId.fromId(message.chat.id),
                            text = "Formato incorrecto. Por favor, usa /log [número de horas]. Ejemplo: /log 7.5",
                        )
                        return@command
                    }

                    val employeeId = "1722334455"

                    val request =
                        NewWorkLogRequest(
                            employeeId = employeeId,
                            hoursWorked = hours,
                        )

                    runBlocking {
                        try {
                            httpClient.post("$apiUrl/api/work-logs") {
                                contentType(ContentType.Application.Json)
                                setBody(request)
                            }
                            bot.sendMessage(
                                ChatId.fromId(message.chat.id),
                                text = "✅ Horas registradas con éxito: $hours horas para el empleado ID $employeeId.",
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

                    // 2. Validamos el formato del periodo con el regex
                    if (!period.matches(periodRegex)) {
                        println(period)
                        bot.sendMessage(
                            ChatId.fromId(message.chat.id),
                            text = "❌ Formato incorrecto. Por favor, usa /solicitar_pago AAAA-MM. Ejemplo: $period",
                        )
                        return@command // Detenemos la ejecución si el formato es inválido
                    }

                    bot.sendMessage(
                        ChatId.fromId(message.chat.id),
                        text = "Procesando tu solicitud para el periodo $period...",
                    )

                    runBlocking {
                        try {
                            // --- LÓGICA PARA LLAMAR A LA API ---
                            // NOTA: Seguimos asumiendo un trabajador fijo para el ejemplo.
                            // En un futuro, aquí buscarías el trabajador asociado a este chat de Telegram.
                            val employeeId = "1722334455" // Cédula del trabajador de ejemplo

                            // a. Obtener el contrato activo
                            // (Necesitarás un endpoint en tu API como GET /api/employees/{id}/contracts/active)
                            // Por ahora, podemos simular que ya tenemos el ID del contrato.
                            val contractId = 2 // ID del contrato activo (ejemplo)

                            // b. Preparar la petición para la API
                            val request =
                                NewPaymentRequest(
                                    employeeId = employeeId,
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
                                    ChatId.fromId(message.chat.id),
                                    text =
                                        """
                                        ✅ ¡Éxito! Tu solicitud de pago para el periodo $period 
                                        ha sido enviada y está pendiente de revisión.
                                        """.trimIndent(),
                                )
                            } else {
                                bot.sendMessage(
                                    ChatId.fromId(message.chat.id),
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
                                ChatId.fromId(message.chat.id),
                                text = "❌ Ocurrió un error de conexión al procesar tu solicitud.",
                            )
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    bot.startPolling()
}
