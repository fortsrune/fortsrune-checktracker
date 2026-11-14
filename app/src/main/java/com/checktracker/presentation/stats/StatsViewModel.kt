package com.checktracker.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checktracker.core.FALLBACK_RATES
import com.checktracker.core.convertMinor
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.model.Expense
import com.checktracker.domain.usecase.ObserveCategoriesUseCase
import com.checktracker.domain.usecase.ObserveCurrencyUseCase
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import com.checktracker.domain.usecase.RefreshRatesUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Период аналитики: неделя или месяц
enum class StatsPeriod(val label: String) {
    WEEK("Неделя"),
    MONTH("Месяц"),
}

// Сумма расходов за один день для столбчатого графика
data class DailyTotal(
    val dayLabel: String,
    val totalMinor: Long,
)

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val displayCurrency: CurrencyOption = CurrencyOption.BYN,
    val totalMinor: Long = 0L,
    // Суммы по категориям, отсортированы по убыванию
    val byCategory: List<Pair<Category, Long>> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
)

class StatsViewModel(
    observeExpensesUseCase: ObserveExpensesUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    observeCurrencyUseCase: ObserveCurrencyUseCase,
    refreshRatesUseCase: RefreshRatesUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(StatsPeriod.WEEK)
    private val rates = MutableStateFlow(FALLBACK_RATES)

    // Общий поток состояния: пересчитывается при изменении любых входных данных
    private val stateFlow: StateFlow<StatsUiState> = combine(
        observeExpensesUseCase(0L, Long.MAX_VALUE),
        observeCategoriesUseCase(),
        observeCurrencyUseCase(),
        rates,
        period,
    ) { expenses, categories, currency, currentRates, currentPeriod ->
        buildState(expenses, categories, currency, currentRates, currentPeriod)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    val state: StateFlow<StatsUiState> = stateFlow

    init {
        viewModelScope.launch {
            refreshRatesUseCase().onSuccess { newRates ->
                rates.value = newRates
            }
        }
    }

    // Переключение периода аналитики
    fun selectPeriod(newPeriod: StatsPeriod) {
        period.value = newPeriod
    }

    private fun buildState(
        expenses: List<Expense>,
        categories: List<Category>,
        currency: CurrencyOption,
        currentRates: Map<String, Double>,
        currentPeriod: StatsPeriod,
    ): StatsUiState {
        // Оставляем только расходы текущего периода и переводим их в выбранную валюту
        val periodStartMillis = periodStart(currentPeriod)
        val converted = expenses
            .filter { it.timestamp >= periodStartMillis }
            .map { expense ->
            expense.copy(
                amountMinor = convertMinor(
                    amountMinor = expense.amountMinor,
                    from = expense.currency,
                    to = currency.code,
                    rates = currentRates,
                ),
            )
        }
        val categoryById = categories.associateBy { it.id }
        return StatsUiState(
            period = currentPeriod,
            displayCurrency = currency,
            totalMinor = converted.sumOf { it.amountMinor },
            byCategory = converted
                .groupBy { it.categoryId }
                .mapValues { (_, list) -> list.sumOf { it.amountMinor } }
                .entries
                .sortedByDescending { it.value }
                .map { (id, sum) -> (categoryById[id] ?: Category(id, "Без категории", "📦")) to sum },
            dailyTotals = buildDailyTotals(converted, currentPeriod),
        )
    }

    // Готовим ряд «день → сумма» от начала периода до сегодня включительно
    private fun buildDailyTotals(expenses: List<Expense>, period: StatsPeriod): List<DailyTotal> {
        val calendar = Calendar.getInstance().apply {
            if (period == StatsPeriod.WEEK) {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            } else {
                set(Calendar.DAY_OF_MONTH, 1)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val labelFormat = SimpleDateFormat("d.MM", Locale("ru"))
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000

        val result = mutableListOf<DailyTotal>()
        while (calendar.timeInMillis <= now) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + dayMillis
            val total = expenses
                .filter { it.timestamp >= dayStart && it.timestamp < dayEnd }
                .sumOf { it.amountMinor }
            result += DailyTotal(dayLabel = labelFormat.format(calendar.time), totalMinor = total)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }

    private fun periodStart(period: StatsPeriod): Long = Calendar.getInstance().apply {
        if (period == StatsPeriod.WEEK) {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        } else {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
