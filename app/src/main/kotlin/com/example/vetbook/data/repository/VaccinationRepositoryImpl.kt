package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteVaccinationDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.data.mappers.toDto
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.domain.models.VaccineSpecies
import com.example.vetbook.domain.models.VaccineTemplates
import com.example.vetbook.domain.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.util.UUID
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

    override suspend fun deleteVaccinationsForPet(petId: String): Result<Unit> = runCatching {
        val all = remoteDataSource.getVaccinationsByPet(petId)
        all.forEach { dto ->
            remoteDataSource.deleteVaccination(dto.id)
        }
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

    override suspend fun generateSchedule(pet: Pet): List<Vaccination> {
        val species = VaccineTemplates.speciesFromPetType(pet.type) ?: return emptyList()
        val birthInstant = pet.birthDate ?: return emptyList()

        return VaccineTemplates.generatableFor(species)
            .sortedBy { it.offsetDays }
            .map { template ->
                val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
val calculated = Instant.ofEpochMilli(birthInstant.toEpochMilli())
    .atZone(java.time.ZoneId.systemDefault())
    .toLocalDate()
    .plusDays(template.offsetDays!!.toLong())
val dateDue = maxOf(calculated, today.plusDays(1))
    .atStartOfDay(java.time.ZoneId.systemDefault())
    .toInstant()

                val now = Instant.now()
                val status = when {
                    dateDue.isBefore(now) -> VaccinationStatus.OVERDUE
                    dateDue.isBefore(now.plus(java.time.Duration.ofDays(7))) -> VaccinationStatus.SCHEDULED
                    else -> VaccinationStatus.SCHEDULED
                }

                Vaccination(
    id = UUID.randomUUID().toString(),
    petId = pet.id,
    ownerId = pet.ownerId ?: "",
    petName = pet.name,
    title = template.name,
    alsoKnownAs = template.alsoKnownAs,
    description = template.description,
    type = template.type,
    offsetDays = template.offsetDays,
    isRecurring = template.isRecurring,
    intervalDays = template.intervalDays,
    lifestyleTrigger = template.lifestyleTrigger,
    scheduledDate = null,
    completedDate = null,
    nextDueDate = null,
    status = VaccinationStatus.PENDING,
    createdAt = Instant.now(),
    updatedAt = Instant.now(),
    reminderEnabled = true,
    reminderDaysBefore = 7
)
            }
        }

    override suspend fun markDone(
        vaccination: Vaccination,
        dateGiven: Instant
    ): Result<Pair<Vaccination, Vaccination?>> = runCatching {
        // 1. Mark the current record done
        val completed = vaccination.copy(
            status = VaccinationStatus.COMPLETED,
            completedDate = dateGiven,
            updatedAt = Instant.now()
        )
        val updateResult = remoteDataSource.updateVaccination(completed.toDto())
        if (updateResult.isFailure) {
            throw updateResult.exceptionOrNull() ?: Exception("Update failed")
        }

        // 2. If recurring, create the next booster dose
        val nextRecord = if (vaccination.isRecurring && vaccination.intervalDays != null) {
            val nextDue = dateGiven
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(vaccination.intervalDays.toLong())
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()

            val next = Vaccination(
                id = UUID.randomUUID().toString(),
                petId = vaccination.petId,
                title = vaccination.title,
                alsoKnownAs = vaccination.alsoKnownAs,
                description = vaccination.description,
                type = vaccination.type,
                offsetDays = vaccination.offsetDays,
                isRecurring = vaccination.isRecurring,
                intervalDays = vaccination.intervalDays,
                lifestyleTrigger = vaccination.lifestyleTrigger,
                scheduledDate = nextDue,
                completedDate = null,
                nextDueDate = null,
                status = VaccinationStatus.SCHEDULED,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                reminderEnabled = vaccination.reminderEnabled,
                reminderDaysBefore = vaccination.reminderDaysBefore
            )
            remoteDataSource.createVaccination(next.toDto())
            next
        } else null

        completed to nextRecord
    }
}