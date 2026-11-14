package com.checktracker.data.repository

import com.checktracker.data.local.dao.AccountDao
import com.checktracker.data.mapper.toDomain
import com.checktracker.data.mapper.toEntity
import com.checktracker.domain.model.Account
import com.checktracker.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val dao: AccountDao,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addAccount(account: Account) {
        dao.insert(account.toEntity())
    }

    override suspend fun deleteAccount(account: Account) {
        dao.delete(account.toEntity())
    }
}
