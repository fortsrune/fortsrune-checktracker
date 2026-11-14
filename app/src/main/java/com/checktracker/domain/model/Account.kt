package com.checktracker.domain.model

// Счёт/кошелёк: наличные, карта банка и т.д.
data class Account(
    val id: Long,
    val name: String,
    val initialBalanceMinor: Long,
    val currency: String,
)
