package com.checktracker.presentation.theme

import androidx.compose.ui.graphics.Color
import com.checktracker.domain.model.Category

// Цвет каждой категории для аватаров и графиков
val Category.color: Color
    get() = when (this) {
        Category.GROCERIES -> Color(0xFF43A047)
        Category.CAFE -> Color(0xFFFB8C00)
        Category.TRANSPORT -> Color(0xFF1E88E5)
        Category.HOME -> Color(0xFF00897B)
        Category.HEALTH -> Color(0xFFE53935)
        Category.ENTERTAINMENT -> Color(0xFF8E24AA)
        Category.OTHER -> Color(0xFF546E7A)
    }
