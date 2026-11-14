package com.checktracker.domain.usecase

import com.checktracker.domain.model.StatementTransaction
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

// Разбор банковской выписки в формате CSV: определяем разделитель, ищем колонки
// по заголовку (рус/англ) и превращаем строки в операции
class ParseStatementUseCase {

    operator fun invoke(text: String): Result<List<StatementTransaction>> = runCatching {
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) throw IllegalArgumentException("Файл пустой")

        val delimiter = if (lines.first().count { it == ';' } >= lines.first().count { it == ',' }) ';' else ','
        val headerCells = splitCsvLine(lines.first(), delimiter).map { it.trim().lowercase() }

        fun findColumn(vararg keys: String): Int =
            headerCells.indexOfFirst { header -> keys.any { header.contains(it) } }

        val dateIdx = findColumn("дата", "date")
        val descIdx = findColumn("описание", "назначение", "операция", "description", "получател", "плательщик")
        val amountIdx = findColumn("сумма", "amount")
        val currencyIdx = findColumn("валют", "currency")
        val hasHeader = amountIdx >= 0
        val dateFormats = listOf(
            "dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd.MM.yyyy",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "dd/MM/yyyy",
        )

        val rows = lines.drop(if (hasHeader) 1 else 0).mapNotNull { line ->
            val cells = splitCsvLine(line, delimiter)
            val amountCell = cells.getOrNull(if (amountIdx >= 0) amountIdx else cells.lastIndex) ?: return@mapNotNull null
            val amount = parseAmount(amountCell) ?: return@mapNotNull null
            if (amount == 0L) return@mapNotNull null

            val description = if (descIdx >= 0) {
                cells.getOrNull(descIdx)?.trim().orEmpty()
            } else {
                cells.getOrNull(1)?.trim().orEmpty()
            }.ifEmpty { "Операция из выписки" }

            val currency = if (currencyIdx >= 0) {
                detectCurrency(cells.getOrNull(currencyIdx).orEmpty())
            } else {
                detectCurrency(line)
            }
            val timestamp = if (dateIdx >= 0) {
                parseDate(cells.getOrNull(dateIdx).orEmpty(), dateFormats)
            } else {
                System.currentTimeMillis()
            }
            RawRow(timestamp, description, amount, currency)
        }

        // Если в выписке есть отрицательные суммы — расходами считаем их, поступления пропускаем
        val expenseRows = if (rows.any { it.amountMinor < 0 }) rows.filter { it.amountMinor < 0 } else rows
        expenseRows.map { row ->
            StatementTransaction(
                timestamp = row.timestamp,
                description = row.description,
                amountMinor = abs(row.amountMinor),
                currency = row.currency,
            )
        }
    }

    private data class RawRow(
        val timestamp: Long,
        val description: String,
        val amountMinor: Long,
        val currency: String,
    )

    // Разбиваем строку CSV с учётом кавычек (внутри них разделители не считаются)
    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    cells += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        cells += current.toString()
        return cells
    }

    // Приводим сумму к минорным единицам: "1 234,56 BYN" -> 123456
    private fun parseAmount(value: String): Long? {
        val cleaned = value
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace(",", ".")
        val number = Regex("-?\\d+(\\.\\d+)?").find(cleaned)?.value ?: return null
        val decimal = number.toDoubleOrNull() ?: return null
        return (decimal * 100).toLong()
    }

    private fun detectCurrency(value: String): String = when {
        value.contains("BYN", ignoreCase = true) || value.contains("Br", ignoreCase = false) -> "BYN"
        value.contains("RUB", ignoreCase = true) || value.contains("₽") -> "RUB"
        value.contains("USD", ignoreCase = true) || value.contains("$") -> "USD"
        else -> "BYN"
    }

    private fun parseDate(value: String, formats: List<String>): Long {
        val cleaned = value.trim().replace("\"", "")
        for (format in formats) {
            try {
                val parser = SimpleDateFormat(format, Locale("ru")).apply { isLenient = false }
                val parsed = parser.parse(cleaned)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
                // Пробуем следующий формат
            }
        }
        return System.currentTimeMillis()
    }
}
