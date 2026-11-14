package com.checktracker.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checktracker.core.formatMoney
import com.checktracker.domain.model.Category
import org.koin.androidx.compose.koinViewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            StatsPeriod.entries.forEachIndexed { index, period ->
                SegmentedButton(
                    selected = state.period == period,
                    onClick = { viewModel.selectPeriod(period) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsPeriod.entries.size),
                ) { Text(period.label) }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Всего за период", style = MaterialTheme.typography.labelMedium)
                Text(
                    formatMoney(state.totalMinor, "RUB"),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        if (state.dailyTotals.isEmpty()) {
            Text("За этот период расходов пока нет.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("По дням", style = MaterialTheme.typography.titleMedium)
            DailyBarChart(dailyTotals = state.dailyTotals)
        }

        if (state.byCategory.isNotEmpty()) {
            Text("По категориям", style = MaterialTheme.typography.titleMedium)
            CategoryBreakdown(byCategory = state.byCategory, totalMinor = state.totalMinor)
        }
    }
}

// Столбчатый график расходов по дням, нарисован на Canvas без сторонних библиотек
@Composable
private fun DailyBarChart(dailyTotals: List<DailyTotal>) {
    val maxTotal = dailyTotals.maxOfOrNull { it.totalMinor } ?: 0L
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            if (dailyTotals.isEmpty() || maxTotal <= 0L) return@Canvas
            val slotWidth = size.width / dailyTotals.size
            val barWidth = slotWidth * 0.6f
            dailyTotals.forEachIndexed { index, day ->
                if (day.totalMinor <= 0L) return@forEachIndexed
                val barHeight = size.height * (day.totalMinor.toFloat() / maxTotal)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(
                        x = index * slotWidth + (slotWidth - barWidth) / 2f,
                        y = size.height - barHeight,
                    ),
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
        }
        // Подписи дней под столбцами
        Row(modifier = Modifier.fillMaxWidth()) {
            dailyTotals.forEach { day ->
                Text(
                    text = day.dayLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
    }
}

// Разбивка по категориям: эмодзи, сумма и полоса-доля от общих расходов
@Composable
private fun CategoryBreakdown(
    byCategory: List<Pair<Category, Long>>,
    totalMinor: Long,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        byCategory.forEach { (category, amountMinor) ->
            val share = if (totalMinor > 0) amountMinor.toFloat() / totalMinor else 0f
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${category.emoji} ${category.label}", style = MaterialTheme.typography.bodyMedium)
                    Text(formatMoney(amountMinor, "RUB"), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { share },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )
            }
        }
    }
}
