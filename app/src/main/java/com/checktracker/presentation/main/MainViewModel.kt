package com.checktracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense
import com.checktracker.domain.usecase.AddExpenseUseCase
import com.checktracker.domain.usecase.DeleteExpenseUseCase
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import com.checktracker.domain.usecase.RefreshRatesUseCase
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val refreshRatesUseCase: RefreshRatesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeExpensesUseCase(startOfMonth(), Long.MAX_VALUE).collect { expenses ->
                _state.update {
                    it.copy(
                        loading = false,
                        expenses = expenses,
                        monthTotalMinor = expenses.sumOf { e -> e.amountMinor },
                        weekTotalMinor = expenses
                            .filter { e -> e.timestamp >= startOfWeek() }
                            .sumOf { e -> e.amountMinor },
                        byCategory = expenses
                            .groupingBy { e -> e.category }
                            .eachSumOf { e -> e.amountMinor },
                    )
                }
            }
        }
        loadRates()
    }

    fun addExpense(merchant: String, amountText: String, category: Category) {
        val amountMinor = amountText.replace(',', '.').toDoubleOrNull()
            ?.let { (it * 100).toLong() } ?: 0L
        val expense = Expense(
            merchant = merchant.trim(),
            amountMinor = amountMinor,
            currency = "RUB",
            category = category,
            timestamp = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            addExpenseUseCase(expense)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { deleteExpenseUseCase(expense) }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun loadRates() {
        viewModelScope.launch {
            refreshRatesUseCase().onSuccess { rates ->
                _state.update { it.copy(rates = rates) }
            }.onFailure {
                // Курсы — не критичные данные: оставляем состояние без пересчёта
            }
        }
    }

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfWeek(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
