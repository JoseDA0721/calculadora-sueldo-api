package com.example

import com.example.dto.NewWorkLogRequest
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
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                    // Usamos triple comilla para un texto de varias líneas
                    val welcomeText =
                        """
                        ¡Hola! Soy tu asistente de registro de horas.
                        Usa /log [horas] para registrar tu trabajo de hoy. Ejemplo: /log 8.5
                        """.trimIndent() // .trimIndent() elimina la indentación del código

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

                    // --- ¡LA MAGIA OCURRE AQUÍ! ---
                    // El bot llama a nuestra propia API para guardar los datos.

                    // NOTA IMPORTANTE: Por ahora, asumimos que el usuario del bot es el empleado con ID=1.
                    // Más adelante, implementaremos un sistema de registro para asociar usuarios de Telegram con empleados.
                    val employeeId = 1
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                    val request =
                        NewWorkLogRequest(
                            employeeId = employeeId,
                            date = today,
                            hours = hours,
                        )

                    // Usamos runBlocking para ejecutar la llamada asíncrona del cliente HTTP
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

                command("resumen") {
                    val employeeId = 1 // Asumimos que es el empleado con ID 1
                    val yearMonth = YearMonth.now()
                    val month = yearMonth.monthValue
                    val year = yearMonth.year

                    val messageText =
                        runBlocking {
                            try {
                                // Llamamos a nuestro nuevo endpoint de la API
                                val response =
                                    httpClient.get("$apiUrl/api/empleado/$employeeId/summary") {
                                        parameter("month", month)
                                        parameter("year", year)
                                    }

                                if (response.status == HttpStatusCode.OK) {
                                    val summary = response.body<MonthlySummaryResponse>()
                                    // Formateamos la respuesta para que sea legible
                                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "EC"))
                                    """
                                    📋 *Resumen de ${summary.month} ${summary.year}*

                                    👤 Empleado ID: `${summary.employeeId}`
                                    ⏰ Horas Totales: *${summary.totalHours} horas*
                                    💲 Tarifa por Hora: *${currencyFormat.format(summary.hourlyRate)}*

                                    💰 *Sueldo Bruto Estimado:* `${currencyFormat.format(summary.totalSalary)}`
                                    """.trimIndent()
                                } else {
                                    "❌ No se pudo generar tu resumen. Código de error: ${response.status.value}"
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                "❌ Ocurrió un error de conexión al generar tu resumen."
                            }
                        }
                    // Enviamos el mensaje final al usuario
                    bot.sendMessage(ChatId.fromId(message.chat.id), text = messageText, parseMode = ParseMode.MARKDOWN)
                }
            }
        }
    bot.startPolling()
}
