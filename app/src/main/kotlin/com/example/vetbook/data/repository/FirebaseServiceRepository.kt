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

    // Icon mapping from service IDs to drawable resources
    private val iconMap = mapOf(
        "cat_vet" to R.drawable.checkup,
        "cat_hotel" to R.drawable.hotel,
        "cat_ride" to R.drawable.homecare,
        "cat_spa" to R.drawable.groom,
        "cat_training" to R.drawable.checkup,
        "cat_party" to R.drawable.hotel,
        "cat_funeral" to R.drawable.groom
    )

    override fun getCategories(): List<ServiceCategory> {
        return runBlocking {
            try {
                val dtos = remoteServiceDataSource.getServiceCategories()
                dtos.map { dto ->
                    val iconRes = iconMap[dto.id] ?: R.drawable.checkup
                    dto.toDomain(iconRes)
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
