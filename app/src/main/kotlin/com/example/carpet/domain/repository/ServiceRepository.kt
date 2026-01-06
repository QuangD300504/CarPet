package com.example.carpet.domain.repository

import com.example.carpet.domain.models.PetServiceDetail
import com.example.carpet.domain.models.ServiceCategory

interface ServiceRepository {
    /**
     * Get the list of main categories to display on ServiceScreen.
     */
    fun getCategories(): List<ServiceCategory>
    fun getServiceDetail(categoryId: String): PetServiceDetail?
}