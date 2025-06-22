package com.example.dto

import com.example.db.PaymentMethods
import kotlinx.serialization.Serializable

// Datos que esperamos recibir en la solicitud JSON
@Serializable
data class NewEmployeeRequest(
    val id: String,
    val firstName: String,
    val lastName: String,
    val activity: String,
    val email: String,
    val method: PaymentMethods? = PaymentMethods.TRANSFERENCIA,
)

data class UpdateEmployeeRequest(
    val activity: String? = null,
    val method: PaymentMethods? = PaymentMethods.TRANSFERENCIA,
    val email: String? = null,
    val telephone: String? = null,
    val telegramChatId: Long? = null,
)

@Serializable
data class EmployeeResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val userId: Int?,
    val activity: String,
    val method: PaymentMethods,
    val email: String,
    val telegramChatId: Long,
)
