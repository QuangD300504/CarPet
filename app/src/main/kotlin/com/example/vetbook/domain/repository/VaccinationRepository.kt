package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for vaccination management
 */
interface VaccinationRepository {
    // CRUD operations
    fun getVaccinationsForPet(petId: String): Flow<List<Vaccination>>
    suspend fun getVaccinationById(id: String): Result<Vaccination>
    suspend fun addVaccination(vaccination: Vaccination): Result<Vaccination>
    suspend fun updateVaccination(vaccination: Vaccination): Result<Unit>
    suspend fun deleteVaccination(vaccinationId: String): Result<Unit>
    
    // Filtering
    suspend fun getUpcomingVaccinations(petId: String): List<Vaccination>
    suspend fun getOverdueVaccinations(petId: String): List<Vaccination>
    suspend fun getCompletedVaccinations(petId: String): List<Vaccination>
    
    // Certificate management
    suspend fun uploadCertificate(vaccinationId: String, imageBytes: ByteArray): Result<String>
}