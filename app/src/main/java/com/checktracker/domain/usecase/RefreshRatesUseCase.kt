package com.checktracker.domain.usecase

import com.checktracker.domain.repository.ExchangeRepository

class RefreshRatesUseCase(private val repository: ExchangeRepository) {
    suspend operator fun invoke(): Result<Map<String, Double>> = repository.getRates()
}
