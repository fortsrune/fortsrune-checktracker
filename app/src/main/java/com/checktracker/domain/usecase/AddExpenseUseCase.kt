package com.checktracker.domain.usecase

import com.checktracker.domain.model.Expense
import com.checktracker.domain.repository.ExpenseRepository

class AddExpenseUseCase(private val repository: ExpenseRepository) {

    suspend operator fun invoke(expense: Expense): Result<Unit> = when {
        expense.merchant.isBlank() -> Result.failure(IllegalArgumentException("Укажите название магазина"))
        expense.amountMinor <= 0 -> Result.failure(IllegalArgumentException("Сумма должна быть больше нуля"))
        else -> {
            repository.addExpense(expense)
            Result.success(Unit)
        }
    }
}
