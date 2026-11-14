package com.checktracker.domain.usecase

import com.checktracker.domain.repository.AccountRepository
import com.checktracker.domain.repository.CategoryRepository
import com.checktracker.domain.repository.ExpenseRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first

class ExportExpensesUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
) {
    // Формируем CSV со всеми расходами: дата, магазин, сумма, валюта, категория, счёт
    suspend operator fun invoke(): Result<String> = runCatching {
        val expenses = expenseRepository.getAllOnce()
        val categories = categoryRepository.observeCategories().first().associateBy { it.id }
        val accounts = accountRepository.observeAccounts().first().associateBy { it.id }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

        val header = "Дата;Магазин;Сумма;Валюта;Категория;Счёт"
        val rows = expenses.map { expense ->
            listOf(
                dateFormat.format(Date(expense.timestamp)),
                escape(expense.merchant),
                formatAmount(expense.amountMinor),
                expense.currency,
                escape(categories[expense.categoryId]?.name ?: "Другое"),
                escape(accounts[expense.accountId]?.name ?: "—"),
            ).joinToString(";")
        }
        (listOf(header) + rows).joinToString("\n")
    }

    private fun escape(value: String): String =
        value.replace(";", ",").replace("\n", " ")

    private fun formatAmount(amountMinor: Long): String {
        val whole = amountMinor / 100
        val cents = amountMinor % 100
        return "$whole.${cents.toString().padStart(2, '0')}"
    }
}
