package com.checktracker.domain.usecase

import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense
import com.checktracker.domain.model.StatementTransaction
import com.checktracker.domain.repository.ExpenseRepository

// Импорт разобранных операций выписки в базу: раскладываем по категориям
// по ключевым словам в описании и привязываем к выбранному счёту
class ImportStatementUseCase(private val expenseRepository: ExpenseRepository) {

    suspend operator fun invoke(
        transactions: List<StatementTransaction>,
        accountId: Long,
        categories: List<Category>,
    ): Result<Int> = runCatching {
        val fallbackCategoryId = categories.firstOrNull { it.name.equals("Другое", ignoreCase = true) }?.id
            ?: categories.lastOrNull()?.id
            ?: 0L

        transactions.forEach { transaction ->
            expenseRepository.addExpense(
                Expense(
                    merchant = transaction.description,
                    amountMinor = transaction.amountMinor,
                    currency = transaction.currency,
                    categoryId = guessCategory(transaction.description, categories, fallbackCategoryId),
                    accountId = accountId,
                    timestamp = transaction.timestamp,
                ),
            )
        }
        transactions.size
    }

    // Простейшая авто-категоризация: ищем ключевые слова в описании операции
    private fun guessCategory(description: String, categories: List<Category>, fallbackId: Long): Long {
        val text = description.lowercase()
        val rules = mapOf(
            "продукт" to listOf("продукт", "супермаркет", "евроопт", "гиппо", "корона", "соседи", "магазин", "дабрабыт", "белмаркет", "сантандер"),
            "кафе" to listOf("кафе", "ресторан", "кофе", "пицц", "столовая", "фуд", "фастфуд", "булочн", "кондитер"),
            "транспорт" to listOf("транспорт", "метро", "автобус", "такси", "заправк", "бензин", "парков", "яндекс го", "транспортн"),
            "жкх" to listOf("жкх", "жку", "квартплат", "водоканал", "энерго", "расчетно", "интернет", "связь", "byfly", "белтелеком", "жилищ"),
            "здоровье" to listOf("аптек", "поликлин", "стомат", "медиц", "анализ", "клиник", "больниц"),
            "развлечени" to listOf("кино", "театр", "развлеч", "игр", "steam", "netflix", "подписк", "боулинг"),
        )
        rules.forEach { (categoryKey, keywords) ->
            val category = categories.firstOrNull { it.name.lowercase().contains(categoryKey) }
            if (category != null && keywords.any { text.contains(it) }) return category.id
        }
        return fallbackId
    }
}
