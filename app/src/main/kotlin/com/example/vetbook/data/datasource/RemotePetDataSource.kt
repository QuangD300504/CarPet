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
    
    /**
     * Create a new pet with owner relationship validation.
     * @param pet The pet data to create
     * @return Result containing the created pet with generated ID, or error
     */
    suspend fun createPet(pet: PetDto): Result<PetDto>
    
    /**
     * Update an existing pet.
     * @param pet The pet data to update (must have valid id)
     * @return Result indicating success or failure
     */
    suspend fun updatePet(pet: PetDto): Result<Unit>
    
    /**
     * Delete a pet by ID.
     * @param petId The ID of the pet to delete
     * @return Result indicating success or failure
     */
    suspend fun deletePet(petId: String): Result<Unit>
    
    /**
     * Get a single pet by ID.
     * @param petId The ID of the pet to retrieve
     * @return Result containing the pet or error
     */
    suspend fun getPetById(petId: String): Result<PetDto>
}
