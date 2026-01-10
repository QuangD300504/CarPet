package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.repository.ServiceRepository

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

