package com.checktracker.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val totalMinor: Long = 0L,
    // Суммы по категориям, отсортированы по убыванию
    val byCategory: List<Pair<Category, Long>> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
)

class StatsViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        selectPeriod(StatsPeriod.WEEK)
    }

    // Переключение периода перезапускает подписку на данные из БД
    fun selectPeriod(newPeriod: StatsPeriod) {
        viewModelScope.launch {
            observeExpensesUseCase(periodStart(newPeriod), Long.MAX_VALUE).collect { expenses ->
                _state.update {
                    StatsUiState(
                        period = newPeriod,
                        totalMinor = expenses.sumOf { e -> e.amountMinor },
                        byCategory = expenses
                            .groupBy { e -> e.category }
                            .mapValues { (_, list) -> list.sumOf { e -> e.amountMinor } }
                            .entries
                            .sortedByDescending { it.value }
                            .map { it.key to it.value },
                        dailyTotals = buildDailyTotals(expenses, newPeriod),
                    )
                }
            }
        }
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
                .sumOf { e -> e.amountMinor }
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
