package com.checktracker.presentation.main

import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense

data class MainUiState(
    val loading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val monthTotalMinor: Long = 0L,
    val weekTotalMinor: Long = 0L,
    val byCategory: Map<Category, Long> = emptyMap(),
    val rates: Map<String, Double> = emptyMap(),
    val error: String? = null,
) {
    val monthTotalUsd: Double?
        get() = rates["USD"]?.takeIf { it > 0 }?.let { (monthTotalMinor / 100.0) / it }
}
