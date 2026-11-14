package com.checktracker.presentation.theme

import androidx.compose.ui.graphics.Color
import com.checktracker.domain.model.Category

// Палитра для аватаров категорий и графиков: цвет подбирается по id категории,
// поэтому работает и для пользовательских категорий
private val PALETTE = listOf(
    Color(0xFF43A047),
    Color(0xFFFB8C00),
    Color(0xFF1E88E5),
    Color(0xFF00897B),
    Color(0xFFE53935),
    Color(0xFF8E24AA),
    Color(0xFF546E7A),
    Color(0xFFF4511E),
    Color(0xFF039BE5),
    Color(0xFF7CB342)
)

// Цвет категории по её id (null — серый нейтральный)
fun categoryColor(category: Category?): Color = if (category == null) {
    Color(0xFF546E7A)
} else {
    PALETTE[(category.id % PALETTE.size).toInt()]
}
