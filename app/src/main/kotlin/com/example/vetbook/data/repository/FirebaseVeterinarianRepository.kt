package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteVeterinarianDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.DoctorReview
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.data.models.DoctorReviewDto
import com.example.vetbook.data.models.VeterinarianDto
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

    override suspend fun getVeterinarianDtoById(id: String): VeterinarianDto? {
        return remoteVeterinarianDataSource.getVeterinarianById(id)
    }

    override suspend fun createVeterinarian(veterinarian: VeterinarianDto): Result<VeterinarianDto> {
        return remoteVeterinarianDataSource.createVeterinarian(veterinarian)
    }

    override suspend fun updateVeterinarian(id: String, fields: Map<String, Any?>) {
        remoteVeterinarianDataSource.updateVeterinarian(id, fields)
    }

    override suspend fun deleteVeterinarian(id: String) {
        remoteVeterinarianDataSource.updateVeterinarian(id, mapOf("isActive" to false))
    }

    override suspend fun submitReview(review: DoctorReview): Result<Unit> {
        val dto = DoctorReviewDto(
            id = review.id,
            appointmentId = review.appointmentId,
            doctorId = review.doctorId,
            userId = review.userId,
            userName = review.userName,
            rating = review.rating,
            comment = review.comment,
            createdAt = review.createdAt
        )
        return remoteVeterinarianDataSource.submitReview(dto)
    }

    override suspend fun getDoctorReviews(doctorId: String): List<DoctorReview> {
        return remoteVeterinarianDataSource.getDoctorReviews(doctorId)
            .map { it.toDomain() }
    }

    override suspend fun updateDoctorRating(doctorId: String) {
        remoteVeterinarianDataSource.updateDoctorRating(doctorId)
    }
}
