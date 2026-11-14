package com.checktracker.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatesResponse(
    val result: String = "",
    val base_code: String = "",
    val rates: Map<String, Double> = emptyMap(),
)
