package com.checktracker.core

import com.checktracker.domain.model.CurrencyOption

// Резервные курсы относительно RUB — применяются, только если API курсов недоступен
val FALLBACK_RATES: Map<String, Double> = mapOf(
    CurrencyOption.RUB.code to 1.0,
    CurrencyOption.USD.code to 90.0,
    CurrencyOption.BYN.code to 30.0,
)

// Пересчёт суммы из одной валюты в другую по таблице курсов относительно RUB
fun convertMinor(amountMinor: Long, from: String, to: String, rates: Map<String, Double>): Long {
    if (from == to) return amountMinor
    val fromRate = rates[from] ?: return amountMinor
    val toRate = rates[to] ?: return amountMinor
    if (toRate == 0.0) return amountMinor
    return (amountMinor * fromRate / toRate).toLong()
}
