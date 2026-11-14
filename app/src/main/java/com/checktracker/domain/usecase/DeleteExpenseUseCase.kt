package com.checktracker.domain.usecase

import com.checktracker.domain.model.Expense
import com.checktracker.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense) = repository.deleteExpense(expense)
}
