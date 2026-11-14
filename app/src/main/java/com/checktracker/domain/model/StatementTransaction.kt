package com.checktracker.domain.model

// Одна операция из банковской выписки, готовая к импорту
data class StatementTransaction(
    val timestamp: Long,
    val description: String,
    val amountMinor: Long,
    val currency: String,
)
