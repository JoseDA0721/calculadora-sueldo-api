package com.example.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewDiscountRequest(
    val contracId: Int,
    val description: String,
    val amount: Double,
    val date: LocalDate,
)

@Serializable
data class DiscountResponse(
    val id: Int,
    val contractId: Int,
    val description: String,
    val amount: Double,
    val date: LocalDate,
)
