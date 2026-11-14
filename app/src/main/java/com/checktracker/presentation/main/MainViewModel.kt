package com.checktracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checktracker.core.FALLBACK_RATES
import com.checktracker.core.convertMinor
import com.checktracker.domain.model.Account
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.model.Expense
import com.checktracker.domain.usecase.AddAccountUseCase
import com.checktracker.domain.usecase.AddCategoryUseCase
import com.checktracker.domain.usecase.AddExpenseUseCase
import com.checktracker.domain.usecase.DeleteExpenseUseCase
import com.checktracker.domain.usecase.ExportExpensesUseCase
import com.checktracker.domain.usecase.ObserveAccountsUseCase
import com.checktracker.domain.usecase.ObserveBudgetUseCase
import com.checktracker.domain.usecase.ObserveCategoriesUseCase
import com.checktracker.domain.usecase.ObserveCurrencyUseCase
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import com.checktracker.domain.usecase.RefreshRatesUseCase
import com.checktracker.domain.usecase.SetBudgetUseCase
import com.checktracker.domain.usecase.SetCurrencyUseCase
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val observeAccountsUseCase: ObserveAccountsUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val observeCurrencyUseCase: ObserveCurrencyUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val observeBudgetUseCase: ObserveBudgetUseCase,
    private val setBudgetUseCase: SetBudgetUseCase,
    private val refreshRatesUseCase: RefreshRatesUseCase,
    private val exportExpensesUseCase: ExportExpensesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    // Локальные UI-потоки: фильтры и курсы
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedAccountFlow = MutableStateFlow<Long?>(null)
    private val selectedCategoryFlow = MutableStateFlow<Long?>(null)
    private val ratesFlow = MutableStateFlow(FALLBACK_RATES)

    init {
        viewModelScope.launch {
            combine(
                observeExpensesUseCase(startOfMonth(), Long.MAX_VALUE),
                observeAccountsUseCase(),
                observeCategoriesUseCase(),
                observeCurrencyUseCase(),
                combine(
                    observeBudgetUseCase(),
                    searchQueryFlow,
                    selectedAccountFlow,
                    selectedCategoryFlow,
                    ratesFlow,
                ) { budget, query, accountId, categoryId, rates ->
                    Filters(budget, query, accountId, categoryId, rates)
                },
            ) { expenses, accounts, categories, currency, filters ->
                buildState(expenses, accounts, categories, currency, filters)
            }.collect { newState ->
                _state.value = newState
            }
        }
        loadRates()
    }

    fun addExpense(merchant: String, amountText: String, categoryId: Long, accountId: Long, currency: CurrencyOption) {
        val amountMinor = amountText.replace(',', '.').toDoubleOrNull()
            ?.let { (it * 100).toLong() } ?: 0L
        val expense = Expense(
            merchant = merchant.trim(),
            amountMinor = amountMinor,
            currency = currency.code,
            categoryId = categoryId,
            accountId = accountId,
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

    fun addAccount(name: String, initialBalanceText: String, currency: CurrencyOption) {
        val balanceMinor = initialBalanceText.replace(',', '.').toDoubleOrNull()
            ?.let { (it * 100).toLong() } ?: 0L
        viewModelScope.launch {
            addAccountUseCase(Account(name = name.trim(), initialBalanceMinor = balanceMinor, currency = currency.code))
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun addCategory(name: String, emoji: String) {
        viewModelScope.launch {
            addCategoryUseCase(Category(id = 0, name = name.trim(), emoji = emoji))
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectAccount(accountId: Long?) {
        selectedAccountFlow.value = accountId
    }

    fun selectCategory(categoryId: Long?) {
        selectedCategoryFlow.value = categoryId
    }

    fun setDisplayCurrency(currency: CurrencyOption) {
        viewModelScope.launch { setCurrencyUseCase(currency) }
    }

    fun setBudget(limitText: String) {
        val limitMinor = limitText.replace(',', '.').toDoubleOrNull()
            ?.let { (it * 100).toLong() } ?: 0L
        viewModelScope.launch { setBudgetUseCase(limitMinor) }
    }

    // Экспорт всех расходов в CSV
    suspend fun exportCsv(): Result<String> = exportExpensesUseCase()

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun loadRates() {
        viewModelScope.launch {
            refreshRatesUseCase().onSuccess { rates ->
                ratesFlow.value = rates
            }.onFailure {
                // API недоступен — остаёмся на резервных курсах
            }
        }
    }

    private fun buildState(
        expenses: List<Expense>,
        accounts: List<Account>,
        categories: List<Category>,
        currency: CurrencyOption,
        filters: Filters,
    ): MainUiState {
        val effectiveRates = filters.rates.ifEmpty { FALLBACK_RATES }
        val converted = expenses.map { expense ->
            expense.copy(
                amountMinor = convertMinor(expense.amountMinor, expense.currency, currency.code, effectiveRates),
            )
        }

        val monthTotal = converted.sumOf { it.amountMinor }
        val weekTotal = converted.filter { it.timestamp >= startOfWeek() }.sumOf { it.amountMinor }
        val todayTotal = converted.filter { it.timestamp >= startOfDay() }.sumOf { it.amountMinor }

        // Баланс счёта = начальный остаток − все расходы по счёту
        val accountBalances = accounts.associate { account ->
            val spent = converted
                .filter { it.accountId == account.id }
                .sumOf { it.amountMinor }
            account.id to (convertMinor(account.initialBalanceMinor, account.currency, currency.code, effectiveRates) - spent)
        }

        // Применяем фильтры: счёт, категория, поиск
        val query = filters.query.trim().lowercase()
        val filtered = converted.filter { expense ->
            (filters.accountId == null || expense.accountId == filters.accountId) &&
                (filters.categoryId == null || expense.categoryId == filters.categoryId) &&
                (query.isEmpty() || expense.merchant.lowercase().contains(query))
        }

        return MainUiState(
            loading = false,
            expenses = filtered,
            accounts = accounts,
            categories = categories,
            accountBalances = accountBalances,
            selectedAccountId = filters.accountId,
            selectedCategoryId = filters.categoryId,
            searchQuery = filters.query,
            displayCurrency = currency,
            monthTotalMinor = monthTotal,
            weekTotalMinor = weekTotal,
            todayTotalMinor = todayTotal,
            budgetMinor = filters.budget,
        )
    }

    private data class Filters(
        val budget: Long,
        val query: String,
        val accountId: Long?,
        val categoryId: Long?,
        val rates: Map<String, Double>,
    )

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
