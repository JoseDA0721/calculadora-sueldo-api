package com.example.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

// Datos que esperamos recibir para registrar horas
@Serializable
data class NewWorkLogRequest(
    val employeeId: String,
    val hoursWorked: Double,
)

// Datos que devolveremos como respuesta
@Serializable
data class WorkLogResponse(
    val id: Int,
    val contractId: Int,
    val date: LocalDate,
    val hoursWorked: Double,
)
