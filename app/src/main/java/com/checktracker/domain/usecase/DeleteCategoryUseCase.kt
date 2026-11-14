package com.checktracker.domain.usecase

import com.checktracker.domain.model.Category
import com.checktracker.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.deleteCategory(category)
}
