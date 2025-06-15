package com.example.dto

import kotlinx.serialization.Serializable

// Datos que esperamos recibir para registrar horas
@Serializable
data class NewWorkLogRequest(
    val employeeId: Int,
    val date: String, // Usaremos String por simplicidad, ej: "2025-06-16"
    val hours: Double
)

// Datos que devolveremos como respuesta
@Serializable
data class WorkLogResponse(
    val id: Int,
    val employeeId: Int,
    val date: String,
    val hours: Double
)