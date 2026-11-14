package com.checktracker.domain.model

// Валюты, доступные в приложении. По умолчанию — BYN (приложение ориентировано на Беларусь)
enum class CurrencyOption(val code: String, val label: String, val symbol: String) {
    BYN("BYN", "Белорусский рубль", "Br"),
    RUB("RUB", "Российский рубль", "₽"),
    USD("USD", "Доллар США", "$");

    companion object {
        fun from(code: String): CurrencyOption =
            entries.firstOrNull { it.code == code } ?: BYN
    }
}
