package com.checktracker.data.repository

import com.checktracker.data.remote.ExchangeRateApi
import com.checktracker.domain.repository.ExchangeRepository

class ExchangeRepositoryImpl(
    private val api: ExchangeRateApi,
) : ExchangeRepository {

    override suspend fun getRates(): Result<Map<String, Double>> = runCatching {
        val response = api.getRates(base = "RUB")
        check(response.result == "success") { "API вернул статус: ${response.result}" }
        response.rates
    }
}
