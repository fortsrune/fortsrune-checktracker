package com.checktracker.di

import com.checktracker.domain.usecase.AddExpenseUseCase
import com.checktracker.domain.usecase.DeleteExpenseUseCase
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import com.checktracker.domain.usecase.RefreshRatesUseCase
import com.checktracker.presentation.main.MainViewModel
import com.checktracker.presentation.stats.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val domainModule = module {
    factory { ObserveExpensesUseCase(get()) }
    factory { AddExpenseUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
    factory { RefreshRatesUseCase(get()) }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::StatsViewModel)
}

val appModules = listOf(databaseModule, networkModule, repositoryModule, domainModule, viewModelModule)
