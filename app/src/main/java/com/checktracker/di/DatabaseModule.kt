package com.checktracker.di

import com.checktracker.data.local.AppDatabase
import com.checktracker.data.local.SettingsStore
import com.checktracker.data.local.dao.AccountDao
import com.checktracker.data.local.dao.CategoryDao
import com.checktracker.data.local.dao.ExpenseDao
import com.checktracker.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.create(androidContext()) }
    single<ExpenseDao> { get<AppDatabase>().expenseDao() }
    single<AccountDao> { get<AppDatabase>().accountDao() }
    single<CategoryDao> { get<AppDatabase>().categoryDao() }
    single<SettingsRepository> { SettingsStore(androidContext()) }
}
