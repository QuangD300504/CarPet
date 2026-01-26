package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.VaccinationRecordDto

/**
 * Remote data source for vaccination records.
 * Vaccinations have relationships with Pet and optionally Veterinarian.
 */
interface RemoteVaccinationDataSource {

    /**
     * Get all vaccinations for a specific pet.
     * @param petId Foreign key to Pet
     * @return List of vaccination records for the pet
     */
    suspend fun getVaccinationsByPet(petId: String): List<VaccinationRecordDto>

    /**
     * Get all vaccinations administered by a specific veterinarian.
     * @param veterinarianId Foreign key to Veterinarian
     * @return List of vaccination records
     */
    suspend fun getVaccinationsByVeterinarian(veterinarianId: String): List<VaccinationRecordDto>

    /**
     * Create a vaccination record with relationship validation.
     * @param vaccination The vaccination data to create
     * @return Result containing the created vaccination or error
     */
    suspend fun createVaccination(vaccination: VaccinationRecordDto): Result<VaccinationRecordDto>

    /**
     * Update a vaccination record.
     * @param vaccination The vaccination data to update (must have valid id)
     * @return Result indicating success or failure
     */
    suspend fun updateVaccination(vaccination: VaccinationRecordDto): Result<Unit>

    /**
     * Delete a vaccination record.
     * @param vaccinationId The ID of the vaccination to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteVaccination(vaccinationId: String): Result<Unit>

    /**
     * Get a single vaccination by ID.
     * @param vaccinationId The ID of the vaccination to retrieve
     * @return Result containing the vaccination or error
     */
    suspend fun getVaccinationById(vaccinationId: String): Result<VaccinationRecordDto>
}

