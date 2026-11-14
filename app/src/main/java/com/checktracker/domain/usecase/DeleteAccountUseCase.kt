package com.checktracker.domain.usecase

import com.checktracker.domain.model.Account
import com.checktracker.domain.repository.AccountRepository

class DeleteAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account) = repository.deleteAccount(account)
}
