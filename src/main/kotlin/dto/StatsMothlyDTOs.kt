package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewStats(
    val contractId: Int,
    val period: String,
    val totalHours: Double,
    val netSalary: Double,
    val totalDiscount: Double,
)

@Serializable
data class Stats(
    val id: Int,
    val contractId: Int,
    val period: String,
    val totalHours: Double,
    val netSalary: Double,
    val totalDiscount: Double,
)
