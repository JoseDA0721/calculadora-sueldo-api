package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateHourlyRateRequest(val rate: Double)