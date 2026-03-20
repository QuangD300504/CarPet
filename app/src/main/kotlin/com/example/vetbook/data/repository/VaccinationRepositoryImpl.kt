package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteVaccinationDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.data.mappers.toDto
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class VaccinationRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteVaccinationDataSource,
    private val imageRepository: CloudinaryImageRepository
) : VaccinationRepository {

    override fun getVaccinationsForPet(petId: String): Flow<List<Vaccination>> = flow {
        val dtoList = remoteDataSource.getVaccinationsByPet(petId)
        val domainList = dtoList.map { it.toDomain() }

        // Auto-update overdue status
        val updated = domainList.map { vaccination ->
            if (vaccination.status == VaccinationStatus.SCHEDULED &&
                vaccination.scheduledDate != null &&
                vaccination.scheduledDate.isBefore(Instant.now())) {
                // Mark as overdue and update in Firebase
                val overdue = vaccination.copy(status = VaccinationStatus.OVERDUE)
                remoteDataSource.updateVaccination(overdue.toDto())
                overdue
            } else {
                vaccination
            }
        }

        emit(updated)
    }

    override suspend fun getVaccinationById(id: String): Result<Vaccination> {
        return remoteDataSource.getVaccinationById(id)
            .map { it.toDomain() }
    }

    override suspend fun addVaccination(vaccination: Vaccination): Result<Vaccination> {
        return remoteDataSource.createVaccination(vaccination.toDto())
            .map { it.toDomain() }
    }

    override suspend fun updateVaccination(vaccination: Vaccination): Result<Unit> {
        return remoteDataSource.updateVaccination(vaccination.toDto())
    }

    override suspend fun deleteVaccination(vaccinationId: String): Result<Unit> {
        return remoteDataSource.deleteVaccination(vaccinationId)
    }

    override suspend fun getUpcomingVaccinations(petId: String): List<Vaccination> {
        val all = remoteDataSource.getVaccinationsByPet(petId)
        return all
            .map { it.toDomain() }
            .filter { it.status == VaccinationStatus.SCHEDULED }
            .filter { it.scheduledDate?.isAfter(Instant.now()) == true }
            .sortedBy { it.scheduledDate }
    }

    override suspend fun getOverdueVaccinations(petId: String): List<Vaccination> {
        val all = remoteDataSource.getVaccinationsByPet(petId)
        return all
            .map { it.toDomain() }
            .filter {
                it.status == VaccinationStatus.OVERDUE ||
                        (it.status == VaccinationStatus.SCHEDULED &&
                                it.scheduledDate != null &&
                                it.scheduledDate.isBefore(Instant.now()))
            }
            .sortedBy { it.scheduledDate }
    }

    override suspend fun getCompletedVaccinations(petId: String): List<Vaccination> {
        val all = remoteDataSource.getVaccinationsByPet(petId)
        return all
            .map { it.toDomain() }
            .filter { it.status == VaccinationStatus.COMPLETED }
            .sortedByDescending { it.completedDate }
    }

    override suspend fun uploadCertificate(
        vaccinationId: String,
        imageBytes: ByteArray
    ): Result<String> {
        return try {
            // Upload to Cloudinary
            val uploadResult = imageRepository.uploadImage(imageBytes)
            val imageUrl = uploadResult.getOrNull()
                ?: return Result.failure(Exception("Failed to upload image"))

            // Update vaccination with certificate URL
            val vaccination = remoteDataSource.getVaccinationById(vaccinationId)
                .getOrNull() ?: return Result.failure(Exception("Vaccination not found"))

            remoteDataSource.updateVaccination(
                vaccination.copy(certificateUrl = imageUrl)
            )

            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}