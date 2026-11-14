package com.checktracker.presentation.main

import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.model.Expense

data class MainUiState(
    val loading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val displayCurrency: CurrencyOption = CurrencyOption.BYN,
    val monthTotalMinor: Long = 0L,
    val weekTotalMinor: Long = 0L,
    val todayTotalMinor: Long = 0L,
    val error: String? = null,
)
