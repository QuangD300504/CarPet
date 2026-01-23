package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.PetServiceDetailDto
import com.example.vetbook.data.models.ServiceCategoryDto

/**
 * Remote source for service categories and details.
 */
interface RemoteServiceDataSource {

    fun getCategories(): List<ServiceCategoryDto>

    fun getServiceDetail(categoryId: String): PetServiceDetailDto?
}


