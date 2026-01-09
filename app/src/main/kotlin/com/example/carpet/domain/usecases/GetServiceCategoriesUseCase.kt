package com.example.carpet.domain.usecases

import com.example.carpet.domain.models.ServiceCategory
import com.example.carpet.domain.repository.ServiceRepository

/**
 * Use case for retrieving service categories.
 */
class GetServiceCategoriesUseCase(
    private val serviceRepository: ServiceRepository
) {
    /**
     * Executes the use case to get all service categories.
     * @return List of service categories
     */
    operator fun invoke(): List<ServiceCategory> {
        return serviceRepository.getCategories()
    }
}

