package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.PetDto

/**
 * Remote data source for pet-related operations.
 */
interface RemotePetDataSource {

    suspend fun getUserPets(ownerId: String): List<PetDto>

    /**
     * Pets that are available for adoption.
     */
    suspend fun getAdoptionPets(): List<PetDto>
}


