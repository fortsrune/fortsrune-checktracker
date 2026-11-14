package com.checktracker.di

import com.checktracker.data.local.AppDatabase
import com.checktracker.data.local.CurrencyStore
import com.checktracker.data.local.dao.ExpenseDao
import com.checktracker.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.create(androidContext()) }
    single<ExpenseDao> { get<AppDatabase>().expenseDao() }
    single<SettingsRepository> { CurrencyStore(androidContext()) }
}
