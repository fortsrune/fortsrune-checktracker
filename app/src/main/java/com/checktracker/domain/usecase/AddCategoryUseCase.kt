package com.checktracker.domain.usecase

import com.checktracker.domain.model.Category
import com.checktracker.domain.repository.CategoryRepository

class AddCategoryUseCase(private val repository: CategoryRepository) {

    suspend operator fun invoke(category: Category): Result<Unit> = when {
        category.name.isBlank() -> Result.failure(IllegalArgumentException("Укажите название категории"))
        else -> {
            repository.addCategory(category)
            Result.success(Unit)
        }
    }
}
