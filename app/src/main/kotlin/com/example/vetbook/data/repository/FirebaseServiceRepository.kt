package com.example.vetbook.data.repository

import com.example.vetbook.R
import com.example.vetbook.data.datasource.RemoteServiceDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.PetServiceDetail
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.repository.ServiceRepository
import kotlinx.coroutines.runBlocking

/**
 * Firebase-backed implementation of ServiceRepository.
 * Fetches service data from Firestore and maps DTOs to domain models.
 * 
 * Note: This implementation uses runBlocking for synchronous methods.
 * Consider updating the domain layer to use suspend functions for better async handling.
 */
class FirebaseServiceRepository(
    private val remoteServiceDataSource: RemoteServiceDataSource
) : ServiceRepository {

    override fun getCategories(): List<ServiceCategory> {
        return runBlocking {
            try {
                val dtos = remoteServiceDataSource.getServiceCategories()
                dtos.map { dto ->
                    // Use a generic fallback icon resource; the mapper will also provide the dynamic iconUrl.
                    dto.toDomain(R.drawable.checkup)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override fun getServiceDetail(categoryId: String): PetServiceDetail? {
        return runBlocking {
            try {
                val detailDto = remoteServiceDataSource.getServiceDetail(categoryId)
                detailDto?.toDomain()
            } catch (e: Exception) {
                null
            }
        }
    }
}
