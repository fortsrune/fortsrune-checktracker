package com.checktracker.domain.model

data class Expense(
    val id: Long = 0L,
    val merchant: String,
    val amountMinor: Long,
    val currency: String,
    val categoryId: Long,
    val accountId: Long,
    val timestamp: Long,
)
