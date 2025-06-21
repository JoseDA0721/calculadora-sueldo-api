package com.example.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewPaymentRequest(
    val contractId: Int,
    val peroid: String,
    val dataRequest: LocalDate
)

@Serializable
data class PaymentRequestResponse(
    val id: Int,
    val contractId: Int,
    val peroid: String,
    val status: String,
    val dateRequest: LocalDate,
    val reviewBy: Int,
    val observation: String
)
