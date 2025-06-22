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

data class UpdatePayment(
    val id: String,
    val status: String,
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
