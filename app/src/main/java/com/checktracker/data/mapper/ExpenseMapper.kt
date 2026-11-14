package com.checktracker.data.mapper

import com.checktracker.data.local.entity.ExpenseEntity
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    merchant = merchant,
    amountMinor = amountMinor,
    currency = currency,
    category = Category.from(category),
    timestamp = timestamp,
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    merchant = merchant,
    amountMinor = amountMinor,
    currency = currency,
    category = category.name,
    timestamp = timestamp,
)
