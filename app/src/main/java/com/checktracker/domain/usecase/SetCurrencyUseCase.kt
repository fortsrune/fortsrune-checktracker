package com.checktracker.domain.usecase

import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.repository.SettingsRepository

class SetCurrencyUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(currency: CurrencyOption) = repository.setCurrency(currency)
}
