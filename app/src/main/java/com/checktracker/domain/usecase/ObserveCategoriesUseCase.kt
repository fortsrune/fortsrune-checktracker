package com.checktracker.domain.usecase

import com.checktracker.domain.model.Category
import com.checktracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repository.observeCategories()
}
