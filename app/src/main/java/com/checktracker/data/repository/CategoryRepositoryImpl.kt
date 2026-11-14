package com.checktracker.data.repository

import com.checktracker.data.local.dao.CategoryDao
import com.checktracker.data.mapper.toDomain
import com.checktracker.domain.model.Category
import com.checktracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addCategory(category: Category) {
        dao.insert(com.checktracker.data.local.entity.CategoryEntity(name = category.name, emoji = category.emoji))
    }

    override suspend fun deleteCategory(category: Category) {
        dao.delete(com.checktracker.data.local.entity.CategoryEntity(id = category.id, name = category.name, emoji = category.emoji))
    }
}
