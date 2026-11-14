package com.checktracker.domain.repository

import com.checktracker.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    suspend fun addAccount(account: Account)
    suspend fun deleteAccount(account: Account)
}
