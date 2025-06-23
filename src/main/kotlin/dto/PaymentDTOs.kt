package com.example.dto

import com.example.db.StatusPayment
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewPayment(
    val requestId: Int,
    val salaryBase: Double,
    val discounts: Double,
    val total: Double,
)

@Serializable
data class UpdatePayment(
    val id: String,
    val status: String,
)

@Serializable
data class EstimatedSalaryResponse(
    val period: String,
    val hourlyRate: Double,
    val totalHours: Double,
    val baseSalary: Double,
    val totalDiscounts: Double,
    val estimatedNetSalary: Double,
)

@Serializable
data class PaymentResponse(
    val id: Int,
    val requestId: Int,
    val salaryBase: Double,
    val discounts: Double,
    val total: Double,
    val status: StatusPayment,
    val paymentDate: LocalDate,
)
