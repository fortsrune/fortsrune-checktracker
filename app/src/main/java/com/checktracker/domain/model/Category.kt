package com.checktracker.domain.model

// Категория расходов: стандартные засеиваются в БД, пользователь может добавлять свои
data class Category(
    val id: Long,
    val name: String,
    val emoji: String,
)
