package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class MonthlySummaryResponse(
    val month: String,
    val year: Int,
    val employeeId: Int,
    val totalHours: Double,
    val hourlyRate: Double,
    val totalSalary: Double,
)
