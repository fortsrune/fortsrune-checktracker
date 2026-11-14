package com.checktracker.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checktracker.core.formatMoney
import com.checktracker.domain.model.Category
import com.checktracker.presentation.theme.color
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
        // Переключатель периода: неделя / месяц
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            StatsPeriod.entries.forEachIndexed { index, period ->
                SegmentedButton(
                    selected = state.period == period,
                    onClick = { viewModel.selectPeriod(period) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsPeriod.entries.size),
                ) { Text(period.label) }
            }
        }

        // Итоговая карточка в стиле главного экрана
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
        ) {
            Column {
                Text(
                    "Всего за период",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatMoney(state.totalMinor, state.displayCurrency.code),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        if (state.dailyTotals.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("📊", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "За этот период расходов пока нет",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Text("По дням", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DailyBarChart(dailyTotals = state.dailyTotals)
        }

        if (state.byCategory.isNotEmpty()) {
            Text("По категориям", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CategoryBreakdown(
                byCategory = state.byCategory,
                totalMinor = state.totalMinor,
                currencyCode = state.displayCurrency.code,
            )
        }
    }
}

// Столбчатый график расходов по дням, нарисован на Canvas без сторонних библиотек
@Composable
private fun DailyBarChart(dailyTotals: List<DailyTotal>) {
    val maxTotal = dailyTotals.maxOfOrNull { it.totalMinor } ?: 0L
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 14.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            if (dailyTotals.isEmpty() || maxTotal <= 0L) return@Canvas
            val slotWidth = size.width / dailyTotals.size
            val barWidth = slotWidth * 0.55f
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
                    cornerRadius = CornerRadius(8f, 8f),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
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

// Разбивка по категориям: аватар, сумма, доля и процент от общих расходов
@Composable
private fun CategoryBreakdown(
    byCategory: List<Pair<Category, Long>>,
    totalMinor: Long,
    currencyCode: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        byCategory.forEach { (category, amountMinor) ->
            val share = if (totalMinor > 0) amountMinor.toFloat() / totalMinor else 0f
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(category.color.copy(alpha = 0.16f))
                        .padding(8.dp),
                ) {
                    Text(category.emoji, fontSize = 16.sp)
                }
                Spacer(Modifier.width(0.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(category.label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${(share * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // Полоса-доля категории, цвет совпадает с цветом категории
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(category.color.copy(alpha = 0.15f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(share)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(category.color),
                        )
                    }
                }
                Text(
                    formatMoney(amountMinor, currencyCode),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
