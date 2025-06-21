package com.example.dto

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
data class NewPayment(
    val requestId: Int,
    val salaryBase: Double,
    val discounts: Double,
    val total: Double,
    val paymentDate: LocalDate
)

data class UpdatePayment(
    val id: String,
    val status: String,
)

data class PaymentResponse(
    val id: Int,
    val requestId: Int,
    val salaryBase: Double,
    val discounts: Double,
    val total: Double,
    val status: String,
    val paymentDate: LocalDate
)