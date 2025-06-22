package com.example.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewContractRequest(
    val employeeId: String,
    val hourlyRate: Double,
)

data class UpdateContractResponse(
    val hourlyRate: Double,
    val endDate: LocalDate,
    val active: Boolean,
)

@Serializable
data class ContractResponse(
    val id: Int,
    val employeeId: String,
    val hourlyRate: Double,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val active: Boolean,
)
