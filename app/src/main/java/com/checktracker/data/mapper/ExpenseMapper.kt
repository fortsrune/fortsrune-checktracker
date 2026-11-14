package com.checktracker.data.mapper

import com.checktracker.data.local.entity.AccountEntity
import com.checktracker.data.local.entity.CategoryEntity
import com.checktracker.data.local.entity.ExpenseEntity
import com.checktracker.domain.model.Account
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    merchant = merchant,
    amountMinor = amountMinor,
    currency = currency,
    categoryId = categoryId,
    accountId = accountId,
    timestamp = timestamp,
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    merchant = merchant,
    amountMinor = amountMinor,
    currency = currency,
    categoryId = categoryId,
    accountId = accountId,
    timestamp = timestamp,
)

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    initialBalanceMinor = initialBalanceMinor,
    currency = currency,
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    initialBalanceMinor = initialBalanceMinor,
    currency = currency,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    emoji = emoji,
)
