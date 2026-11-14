package com.checktracker.domain.repository

import com.checktracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(from: Long, to: Long): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}
