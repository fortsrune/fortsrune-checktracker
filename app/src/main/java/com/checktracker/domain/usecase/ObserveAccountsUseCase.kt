package com.checktracker.domain.usecase

import com.checktracker.domain.model.Account
import com.checktracker.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAccounts()
}
