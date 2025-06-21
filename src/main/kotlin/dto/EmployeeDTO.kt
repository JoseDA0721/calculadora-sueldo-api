package com.example.dto

import kotlinx.serialization.Serializable

// Datos que esperamos recibir en la solicitud JSON
@Serializable
data class NewEmployeeRequest(
    val id : String,
    val firstName: String,
    val lastName: String,
    val activiti: String,
    val email: String
)

data class UpdateEmployeeRequest(
    val userId: Int,
    val activiti: String,
    val method: String,
    val email: String,
    val telephone: String,
    val telegramChatId: Long?
)

@Serializable
data class EmployeeResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val userId: Int?,
    val activiti: String,
    val method: String,
    val email: String,
    val telegramChatId: Long?,
)
