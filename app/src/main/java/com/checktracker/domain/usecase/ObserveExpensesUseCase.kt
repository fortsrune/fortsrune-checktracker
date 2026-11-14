package com.checktracker.domain.usecase

import com.checktracker.domain.model.Expense
import com.checktracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(from: Long, to: Long): Flow<List<Expense>> =
        repository.observeExpenses(from, to)
}
