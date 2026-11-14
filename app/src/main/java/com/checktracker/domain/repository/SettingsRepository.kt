package com.checktracker.domain.repository

import com.checktracker.domain.model.CurrencyOption
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeCurrency(): Flow<CurrencyOption>
    suspend fun setCurrency(currency: CurrencyOption)
    fun observeBudget(): Flow<Long>
    suspend fun setBudget(limitMinor: Long)
}
