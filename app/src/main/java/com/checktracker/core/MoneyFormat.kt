package com.checktracker.core

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatMoney(amountMinor: Long, currencyCode: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru"))
    runCatching { format.currency = Currency.getInstance(currencyCode) }
    return format.format(amountMinor / 100.0)
}
