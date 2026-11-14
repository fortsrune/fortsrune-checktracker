package com.checktracker.di

import com.checktracker.data.repository.AccountRepositoryImpl
import com.checktracker.data.repository.CategoryRepositoryImpl
import com.checktracker.data.repository.ExpenseRepositoryImpl
import com.checktracker.data.repository.ExchangeRepositoryImpl
import com.checktracker.domain.repository.AccountRepository
import com.checktracker.domain.repository.CategoryRepository
import com.checktracker.domain.repository.ExpenseRepository
import com.checktracker.domain.repository.ExchangeRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ExpenseRepository> { ExpenseRepositoryImpl(dao = get()) }
    single<ExchangeRepository> { ExchangeRepositoryImpl(api = get()) }
    single<AccountRepository> { AccountRepositoryImpl(dao = get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(dao = get()) }
}
