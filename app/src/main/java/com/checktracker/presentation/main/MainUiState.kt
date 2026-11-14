package com.checktracker.presentation.main

import com.checktracker.domain.model.Account
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.model.Expense

data class MainUiState(
    val loading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    // Баланс каждого счёта: начальный остаток минус расходы (в валюте отображения)
    val accountBalances: Map<Long, Long> = emptyMap(),
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val displayCurrency: CurrencyOption = CurrencyOption.BYN,
    val monthTotalMinor: Long = 0L,
    val weekTotalMinor: Long = 0L,
    val todayTotalMinor: Long = 0L,
    // Месячный лимит расходов, 0 = не задан
    val budgetMinor: Long = 0L,
    val error: String? = null,
)
