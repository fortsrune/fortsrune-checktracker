package com.checktracker.di

import com.checktracker.domain.usecase.AddAccountUseCase
import com.checktracker.domain.usecase.AddCategoryUseCase
import com.checktracker.domain.usecase.AddExpenseUseCase
import com.checktracker.domain.usecase.DeleteExpenseUseCase
import com.checktracker.domain.usecase.ExportExpensesUseCase
import com.checktracker.domain.usecase.ObserveAccountsUseCase
import com.checktracker.domain.usecase.ObserveBudgetUseCase
import com.checktracker.domain.usecase.ObserveCategoriesUseCase
import com.checktracker.domain.usecase.ObserveCurrencyUseCase
import com.checktracker.domain.usecase.ObserveExpensesUseCase
import com.checktracker.domain.usecase.RefreshRatesUseCase
import com.checktracker.domain.usecase.SetBudgetUseCase
import com.checktracker.domain.usecase.SetCurrencyUseCase
import com.checktracker.presentation.main.MainViewModel
import com.checktracker.presentation.stats.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val domainModule = module {
    factory { ObserveExpensesUseCase(get()) }
    factory { AddExpenseUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
    factory { RefreshRatesUseCase(get()) }
    factory { ObserveCurrencyUseCase(get()) }
    factory { SetCurrencyUseCase(get()) }
    factory { ObserveAccountsUseCase(get()) }
    factory { AddAccountUseCase(get()) }
    factory { ObserveCategoriesUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { ObserveBudgetUseCase(get()) }
    factory { SetBudgetUseCase(get()) }
    factory { ExportExpensesUseCase(get(), get(), get()) }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::StatsViewModel)
}

val appModules = listOf(databaseModule, networkModule, repositoryModule, domainModule, viewModelModule)
