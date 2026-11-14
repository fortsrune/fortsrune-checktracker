package com.checktracker.domain.repository

interface ExchangeRepository {
    suspend fun getRates(): Result<Map<String, Double>>
}
