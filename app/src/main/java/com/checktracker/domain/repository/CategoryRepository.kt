package com.checktracker.domain.repository

import com.checktracker.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(category: Category)
}
