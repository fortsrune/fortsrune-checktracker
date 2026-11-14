package com.checktracker.domain.usecase

import com.checktracker.domain.repository.SettingsRepository

class SetBudgetUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(limitMinor: Long) = repository.setBudget(limitMinor)
}
