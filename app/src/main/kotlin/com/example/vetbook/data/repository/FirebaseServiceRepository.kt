package com.example.vetbook.data.repository

import com.example.vetbook.R
import com.example.vetbook.data.datasource.RemoteServiceDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.PetServiceDetail
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.repository.ServiceRepository
import kotlinx.coroutines.runBlocking

class FirebaseServiceRepository(
    private val remoteServiceDataSource: RemoteServiceDataSource
) : ServiceRepository {

    // Only cat_vet is active. cat_shop routes directly to the Store tab
    // via handleServiceNavigation() and never goes through this repository.
    private val allowedServiceIds = setOf("cat_vet")

    override fun getCategories(): List<ServiceCategory> {
        return runBlocking {
            try {
                remoteServiceDataSource.getServiceCategories()
                    .filter { it.id in allowedServiceIds }
                    .map { dto -> dto.toDomain(R.drawable.checkup) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override fun getServiceDetail(categoryId: String): PetServiceDetail? {
        return runBlocking {
            try {
                remoteServiceDataSource.getServiceDetail(categoryId)?.toDomain()
            } catch (e: Exception) {
                null
            }
        }
    }
}