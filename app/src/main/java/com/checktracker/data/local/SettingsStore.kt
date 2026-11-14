package com.checktracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

// Хранилище настроек: валюта отображения и месячный лимит расходов
class SettingsStore(private val context: Context) : SettingsRepository {

    private val currencyKey = stringPreferencesKey("display_currency")
    private val budgetKey = longPreferencesKey("budget_minor")

    override fun observeCurrency(): Flow<CurrencyOption> =
        context.dataStore.data.map { prefs ->
            CurrencyOption.from(prefs[currencyKey] ?: CurrencyOption.BYN.code)
        }

    override suspend fun setCurrency(currency: CurrencyOption) {
        context.dataStore.edit { prefs -> prefs[currencyKey] = currency.code }
    }

    override fun observeBudget(): Flow<Long> =
        context.dataStore.data.map { prefs -> prefs[budgetKey] ?: 0L }

    override suspend fun setBudget(limitMinor: Long) {
        context.dataStore.edit { prefs -> prefs[budgetKey] = limitMinor }
    }
}
