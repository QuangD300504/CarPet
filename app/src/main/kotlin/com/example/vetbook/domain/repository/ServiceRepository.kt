package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.PetServiceDetail
import com.example.vetbook.domain.models.ServiceCategory

interface ServiceRepository {
    /**
     * Get the list of main categories to display on ServiceScreen.
     */
    fun getCategories(): List<ServiceCategory>
    fun getServiceDetail(categoryId: String): PetServiceDetail?
}