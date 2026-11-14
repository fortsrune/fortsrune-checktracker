package com.checktracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checktracker.core.FALLBACK_RATES
import com.checktracker.core.convertMinor
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.model.Expense
import com.checktracker.domain.usecase.AddExpenseUseCase
import com.checktracker.domain.usecase.DeleteExpenseUseCase
import com.checktracker.domain.usecase.ObserveCurrencyUseCase
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import com.checktracker.domain.usecase.RefreshRatesUseCase
import com.checktracker.domain.usecase.SetCurrencyUseCase
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
    private val observeCurrencyUseCase: ObserveCurrencyUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private var cachedExpenses: List<Expense> = emptyList()
    private var rates: Map<String, Double> = emptyMap()
    private var displayCurrency = CurrencyOption.BYN

    init {
        viewModelScope.launch {
            observeCurrencyUseCase().collect { currency ->
                displayCurrency = currency
                recompute()
            }
        }
        viewModelScope.launch {
            observeExpensesUseCase(startOfMonth(), Long.MAX_VALUE).collect { expenses ->
                cachedExpenses = expenses
                recompute()
            }
        }
        loadRates()
    }

    fun addExpense(merchant: String, amountText: String, category: Category, currency: CurrencyOption) {
        val amountMinor = amountText.replace(',', '.').toDoubleOrNull()
            ?.let { (it * 100).toLong() } ?: 0L
        val expense = Expense(
            merchant = merchant.trim(),
            amountMinor = amountMinor,
            currency = currency.code,
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

    fun setDisplayCurrency(currency: CurrencyOption) {
        viewModelScope.launch { setCurrencyUseCase(currency) }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    // Пересчитываем все итоги в выбранную валюту
    private fun recompute() {
        val effectiveRates = rates.ifEmpty { FALLBACK_RATES }
        val converted = cachedExpenses.map { expense ->
            expense.copy(
                amountMinor = convertMinor(
                    amountMinor = expense.amountMinor,
                    from = expense.currency,
                    to = displayCurrency.code,
                    rates = effectiveRates,
                ),
            )
        }
        _state.update {
            it.copy(
                loading = false,
                expenses = cachedExpenses,
                displayCurrency = displayCurrency,
                monthTotalMinor = converted.sumOf { e -> e.amountMinor },
                weekTotalMinor = converted
                    .filter { e -> e.timestamp >= startOfWeek() }
                    .sumOf { e -> e.amountMinor },
                todayTotalMinor = converted
                    .filter { e -> e.timestamp >= startOfDay() }
                    .sumOf { e -> e.amountMinor },
            )
        }
    }

    private fun loadRates() {
        viewModelScope.launch {
            refreshRatesUseCase().onSuccess { newRates ->
                rates = newRates
                recompute()
            }.onFailure {
                // API недоступен — работаем на резервных курсах
                recompute()
            }
        }
    }

    private fun startOfDay(): Long = Calendar.getInstance().apply {
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

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
