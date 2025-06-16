package com.example.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.text.NumberFormat
import java.util.Locale

class NotificationService(private val botToken: String) {
    private val httpClient = HttpClient(CIO)
    private val telegramApiUrl = "https://api.telegram.org/bot$botToken"

    suspend fun sendPaymentNotification(
        chatId: Long,
        amount: Double,
    ) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "EC"))
        val message = "🎉 ¡Buenas noticias! Hemos procesado tu pago por un monto de *${currencyFormat.format(
            amount,
        )}*. ¡Gracias por tu trabajo!"

        try {
            val response: HttpResponse =
                httpClient.post("$telegramApiUrl/sendMessage") {
                    contentType(ContentType.Application.Json)
                    url {
                        parameters.append("chat_id", chatId.toString())
                        parameters.append("text", message)
                        parameters.append("parse_mode", "Markdown")
                    }
                }
            if (response.status != HttpStatusCode.OK) {
                println("Error al enviar notificación a Telegram: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
