package com.checktracker.domain.usecase

import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrencyUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<CurrencyOption> = repository.observeCurrency()
}
