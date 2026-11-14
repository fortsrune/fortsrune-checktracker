package com.checktracker.domain.model

enum class Category(val label: String, val emoji: String) {
    GROCERIES("Продукты", "🛒"),
    CAFE("Кафе и рестораны", "☕"),
    TRANSPORT("Транспорт", "🚌"),
    HOME("Дом и ЖКХ", "🏠"),
    HEALTH("Здоровье", "💊"),
    ENTERTAINMENT("Развлечения", "🎬"),
    OTHER("Другое", "📦");

    companion object {
        fun from(name: String): Category =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
