package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.DoctorReviewDto
import com.example.vetbook.data.models.VeterinarianDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source for veterinarian data.
 * Provides methods to fetch and manage veterinarian information from Firestore.
 */
interface RemoteVeterinarianDataSource {

    /**
     * Observe all active veterinarians in real-time.
     * @return Flow emitting list of veterinarians as data changes
     */
    fun observeVeterinarians(): Flow<List<VeterinarianDto>>

    /**
     * Get a specific veterinarian by ID.
     * @param id The veterinarian's unique identifier
     * @return The veterinarian data or null if not found
     */
    suspend fun getVeterinarianById(id: String): VeterinarianDto?

    /**
     * Create a new veterinarian record.
     * @param veterinarian The veterinarian data to create
     * @return Result containing the created veterinarian or error
     */
    suspend fun createVeterinarian(veterinarian: VeterinarianDto): Result<VeterinarianDto>

    /**
     * Update specific fields of a veterinarian record.
     * @param id The veterinarian's unique identifier
     * @param fields Map of field names to new values
     */
    suspend fun updateVeterinarian(id: String, fields: Map<String, Any?>)

    /**
     * Submit a review for a doctor.
     */
    suspend fun submitReview(review: DoctorReviewDto): Result<Unit>

    /**
     * Get all reviews for a specific doctor.
     */
    suspend fun getDoctorReviews(doctorId: String): List<DoctorReviewDto>

    /**
     * Recalculate and update the doctor's average rating and review count
     * after a new review is submitted.
     */
    suspend fun updateDoctorRating(doctorId: String)
}
