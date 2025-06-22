package com.example.dto

import com.example.db.StatusRequestPayment
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewPaymentRequest(
    val employeeId: String,
    val period: String,
)

@Serializable
data class PaymentRequestResponse(
    val id: Int,
    val contractId: Int,
    val period: String,
    val status: StatusRequestPayment,
    val dateRequest: LocalDate,
    val reviewBy: Int?,
    val observation: String?,
)
