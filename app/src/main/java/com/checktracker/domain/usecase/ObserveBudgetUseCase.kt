package com.checktracker.domain.usecase

import com.checktracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveBudgetUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Long> = repository.observeBudget()
}
