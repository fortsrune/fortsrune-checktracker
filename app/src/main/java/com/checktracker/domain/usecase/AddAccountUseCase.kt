package com.checktracker.domain.usecase

import com.checktracker.domain.model.Account
import com.checktracker.domain.repository.AccountRepository

class AddAccountUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(account: Account): Result<Unit> = when {
        account.name.isBlank() -> Result.failure(IllegalArgumentException("Укажите название счёта"))
        else -> {
            repository.addAccount(account)
            Result.success(Unit)
        }
    }
}
