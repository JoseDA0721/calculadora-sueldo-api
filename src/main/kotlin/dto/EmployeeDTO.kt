package com.example.dto

import kotlinx.serialization.Serializable

// Datos que esperamos recibir en la solicitud JSON
@Serializable
data class NewEmployeeRequest(
    val id : String,
    val firstName: String,
    val lastName: String,
    val activiti: String,
    val email: String,
    val telegramChatId: Long? = null,
)

// Datos que enviaremos como respuesta JSON
@Serializable
data class EmployeeResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val telegramChatId: Long?,
)
