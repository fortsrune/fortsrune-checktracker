package com.checktracker.data.repository

import com.checktracker.data.local.dao.ExpenseDao
import com.checktracker.data.mapper.toDomain
import com.checktracker.data.mapper.toEntity
import com.checktracker.domain.model.Expense
import com.checktracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
) : ExpenseRepository {

    override fun observeExpenses(from: Long, to: Long): Flow<List<Expense>> =
        dao.observeBetween(from, to).map { list -> list.map { it.toDomain() } }

    override suspend fun getAllOnce(): List<Expense> = dao.getAllOnce().map { it.toDomain() }

    override suspend fun addExpense(expense: Expense) {
        dao.insert(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        dao.delete(expense.toEntity())
    }
}
