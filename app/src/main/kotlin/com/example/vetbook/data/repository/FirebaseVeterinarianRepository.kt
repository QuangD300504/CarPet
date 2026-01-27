package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteVeterinarianDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.domain.repository.VeterinarianRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Firebase-backed implementation of VeterinarianRepository.
 * Fetches veterinarian data from Firestore and maps DTOs to domain models.
 */
class FirebaseVeterinarianRepository(
    private val remoteVeterinarianDataSource: RemoteVeterinarianDataSource
) : VeterinarianRepository {

    override fun getVeterinarians(): Flow<List<Veterinarian>> {
        return remoteVeterinarianDataSource.observeVeterinarians()
            .map { dtos -> 
                dtos.map { dto -> dto.toDomain() }
            }
    }
}
