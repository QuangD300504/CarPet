package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteVaccinationDataSource
import com.example.vetbook.data.models.VaccinationRecordDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val VACCINATIONS_COLLECTION = "vaccinations"
private const val PETS_COLLECTION = "pets"
private const val VETERINARIANS_COLLECTION = "veterinarians"

/**
 * Firebase implementation of RemoteVaccinationDataSource.
 * Enhanced to support new vaccination fields.
 */
class FirebaseVaccinationDataSource(
    private val firestore: FirebaseFirestore
) : RemoteVaccinationDataSource {

    override suspend fun getVaccinationsByPet(petId: String): List<VaccinationRecordDto> {
        return try {
            val snapshot = firestore
                .collection(VACCINATIONS_COLLECTION)
                .whereEqualTo("petId", petId)
                .orderBy("scheduledDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                doc.toVaccinationDto()
            }
        } catch (e: Exception) {
            // Fallback to older ordering if scheduledDate doesn't exist
            try {
                val snapshot = firestore
                    .collection(VACCINATIONS_COLLECTION)
                    .whereEqualTo("petId", petId)
                    .get()
                    .await()

                snapshot.documents
                    .map { it.toVaccinationDto() }
                    .sortedByDescending { it.scheduledDate ?: it.completedDate ?: it.date ?: 0L }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getVaccinationsByVeterinarian(veterinarianId: String): List<VaccinationRecordDto> {
        return try {
            val snapshot = firestore
                .collection(VACCINATIONS_COLLECTION)
                .whereEqualTo("veterinarianId", veterinarianId)
                .orderBy("scheduledDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                doc.toVaccinationDto()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createVaccination(vaccination: VaccinationRecordDto): Result<VaccinationRecordDto> {
        return try {
            if (vaccination.petId.isBlank()) {
                return Result.failure(Exception("Pet ID is required"))
            }

            // val petDoc = firestore.collection(PETS_COLLECTION)
            //     .document(vaccination.petId)
            //     .get()
            //     .await()

            // if (!petDoc.exists()) {
            //     return Result.failure(Exception("Pet not found: ${vaccination.petId}"))
            // }

            if (vaccination.veterinarianId != null && vaccination.veterinarianId.isNotBlank()) {
                val vetDoc = firestore.collection(VETERINARIANS_COLLECTION)
                    .document(vaccination.veterinarianId)
                    .get()
                    .await()

                if (!vetDoc.exists()) {
                    return Result.failure(Exception("Veterinarian not found: ${vaccination.veterinarianId}"))
                }
            }

            val docRef = if (vaccination.id.isBlank()) {
                firestore.collection(VACCINATIONS_COLLECTION).document()
            } else {
                firestore.collection(VACCINATIONS_COLLECTION).document(vaccination.id)
            }

            val now = System.currentTimeMillis()
            val vaccinationData = vaccination.copy(
                id = docRef.id,
                createdAt = now,
                updatedAt = now
            )

            docRef.set(vaccinationData.toMap()).await()
            Result.success(vaccinationData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVaccination(vaccination: VaccinationRecordDto): Result<Unit> {
        return try {
            if (vaccination.id.isBlank()) {
                return Result.failure(Exception("Vaccination ID is required for update"))
            }

            // if (vaccination.petId.isNotBlank()) {
            //     val petDoc = firestore.collection(PETS_COLLECTION)
            //         .document(vaccination.petId)
            //         .get()
            //         .await()

            //     if (!petDoc.exists()) {
            //         return Result.failure(Exception("Pet not found: ${vaccination.petId}"))
            //     }
            // }

            if (vaccination.veterinarianId != null && vaccination.veterinarianId.isNotBlank()) {
                val vetDoc = firestore.collection(VETERINARIANS_COLLECTION)
                    .document(vaccination.veterinarianId)
                    .get()
                    .await()

                if (!vetDoc.exists()) {
                    return Result.failure(Exception("Veterinarian not found: ${vaccination.veterinarianId}"))
                }
            }

            val updatedData = vaccination.copy(
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(VACCINATIONS_COLLECTION)
                .document(vaccination.id)
                .update(updatedData.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVaccination(vaccinationId: String): Result<Unit> {
        return try {
            if (vaccinationId.isBlank()) {
                return Result.failure(Exception("Vaccination ID is required"))
            }

            firestore.collection(VACCINATIONS_COLLECTION)
                .document(vaccinationId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccinationById(vaccinationId: String): Result<VaccinationRecordDto> {
        return try {
            if (vaccinationId.isBlank()) {
                return Result.failure(Exception("Vaccination ID is required"))
            }

            val doc = firestore.collection(VACCINATIONS_COLLECTION)
                .document(vaccinationId)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("Vaccination not found: $vaccinationId"))
            }

            Result.success(doc.toVaccinationDto())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Enhanced extension functions for data conversion
private fun com.google.firebase.firestore.DocumentSnapshot.toVaccinationDto(): VaccinationRecordDto {
    return VaccinationRecordDto(
        id = id,
        petId = getString("petId") ?: "",
        veterinarianId = getString("veterinarianId"),
        veterinarianName = getString("veterinarianName"),
        clinicName = getString("clinicName"),

        title = getString("title") ?: "",
        type = getString("type") ?: "CORE",
        manufacturer = getString("manufacturer"),
        batchNumber = getString("batchNumber"),

        status = getString("status") ?: "SCHEDULED",
        scheduledDate = getLong("scheduledDate"),
        completedDate = getLong("completedDate"),
        nextDueDate = getLong("nextDueDate"),

        certificateUrl = getString("certificateUrl"),
        notes = getString("notes"),
        sideEffects = getString("sideEffects"),

        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L,

        reminderEnabled = getBoolean("reminderEnabled") ?: true,
        reminderDaysBefore = getLong("reminderDaysBefore")?.toInt() ?: 7,

        isCompleted = getBoolean("isCompleted") ?: false,
        date = getLong("date")
    )
}

private fun VaccinationRecordDto.toMap(): Map<String, Any?> {
    return buildMap {
        put("petId", petId)
        put("veterinarianId", veterinarianId)
        put("veterinarianName", veterinarianName)
        put("clinicName", clinicName)

        put("title", title)
        put("type", type)
        put("manufacturer", manufacturer)
        put("batchNumber", batchNumber)

        put("status", status)
        put("scheduledDate", scheduledDate)
        put("completedDate", completedDate)
        put("nextDueDate", nextDueDate)

        put("certificateUrl", certificateUrl)
        put("notes", notes)
        put("sideEffects", sideEffects)

        put("createdAt", createdAt)
        put("updatedAt", updatedAt)

        put("reminderEnabled", reminderEnabled)
        put("reminderDaysBefore", reminderDaysBefore)

        put("isCompleted", status == "COMPLETED")
        put("date", scheduledDate ?: completedDate)
    }
}