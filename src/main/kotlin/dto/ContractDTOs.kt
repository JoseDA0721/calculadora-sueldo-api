package com.example.dto

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
data class NewContractRequest(
    val employeeId: String,
    val hourlyRate: Double,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class UpdateContractResponse(
    val hourlyRate: Double,
    val endDate: LocalDate,
    val active: Boolean
)

@Serializable
data class ContractResponse(
    val id: Int,
    val employeeId: String,
    val hourlyRate: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val active: Boolean,
    val createdAt: String
)