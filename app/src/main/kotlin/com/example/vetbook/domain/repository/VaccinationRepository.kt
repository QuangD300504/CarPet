package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

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

    /**
     * Generates a list of Vaccination records from WSAVA-aligned templates
     * for the given pet. Does NOT persist them — use addVaccination for each.
     *
     * @param pet the pet whose schedule to generate (requires pet.type + birthDate)
     * @return list of unsaved Vaccination records, sorted by scheduledDate
     */
    suspend fun generateSchedule(pet: Pet): List<Vaccination>

    /**
     * Marks a vaccination as done, and if it is recurring, automatically creates
     * the next booster dose.
     *
     * @param vaccination the vaccination record to mark complete
     * @param dateGiven the date the vaccine was administered (defaults to now)
     * @return Result containing the updated record and optionally the new next-dose record
     */
    suspend fun markDone(vaccination: Vaccination, dateGiven: Instant = Instant.now()): Result<Pair<Vaccination, Vaccination?>>

    /**
     * Deletes ALL vaccination records for a given pet.
     * Used when the user skips/closes the vaccine review modal without confirming.
     */
    suspend fun deleteVaccinationsForPet(petId: String): Result<Unit>
}