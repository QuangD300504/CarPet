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
 * Handles vaccination records with relationships to Pet and Veterinarian.
 */
class FirebaseVaccinationDataSource(
    private val firestore: FirebaseFirestore
) : RemoteVaccinationDataSource {

    override suspend fun getVaccinationsByPet(petId: String): List<VaccinationRecordDto> {
        return try {
            val snapshot = firestore
                .collection(VACCINATIONS_COLLECTION)
                .whereEqualTo("petId", petId) // Query by foreign key
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                doc.toVaccinationDto()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getVaccinationsByVeterinarian(veterinarianId: String): List<VaccinationRecordDto> {
        return try {
            val snapshot = firestore
                .collection(VACCINATIONS_COLLECTION)
                .whereEqualTo("veterinarianId", veterinarianId) // Query by foreign key
                .orderBy("date", Query.Direction.DESCENDING)
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
            // Validate pet exists (required foreign key)
            if (vaccination.petId.isBlank()) {
                return Result.failure(Exception("Pet ID is required"))
            }

            val petDoc = firestore.collection(PETS_COLLECTION)
                .document(vaccination.petId)
                .get()
                .await()

            if (!petDoc.exists()) {
                return Result.failure(Exception("Pet not found: ${vaccination.petId}"))
            }

            // Validate veterinarian exists if provided (optional foreign key)
            if (vaccination.veterinarianId != null && vaccination.veterinarianId.isNotBlank()) {
                val vetDoc = firestore.collection(VETERINARIANS_COLLECTION)
                    .document(vaccination.veterinarianId)
                    .get()
                    .await()

                if (!vetDoc.exists()) {
                    return Result.failure(Exception("Veterinarian not found: ${vaccination.veterinarianId}"))
                }
            }

            // Create vaccination document
            val docRef = if (vaccination.id.isBlank()) {
                firestore.collection(VACCINATIONS_COLLECTION).document()
            } else {
                firestore.collection(VACCINATIONS_COLLECTION).document(vaccination.id)
            }

            val now = System.currentTimeMillis()
            val vaccinationData = vaccination.copy(
                id = docRef.id,
                createdAt = now
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

            // Validate pet exists if petId is being changed
            if (vaccination.petId.isNotBlank()) {
                val petDoc = firestore.collection(PETS_COLLECTION)
                    .document(vaccination.petId)
                    .get()
                    .await()

                if (!petDoc.exists()) {
                    return Result.failure(Exception("Pet not found: ${vaccination.petId}"))
                }
            }

            // Validate veterinarian exists if veterinarianId is being changed
            if (vaccination.veterinarianId != null && vaccination.veterinarianId.isNotBlank()) {
                val vetDoc = firestore.collection(VETERINARIANS_COLLECTION)
                    .document(vaccination.veterinarianId)
                    .get()
                    .await()

                if (!vetDoc.exists()) {
                    return Result.failure(Exception("Veterinarian not found: ${vaccination.veterinarianId}"))
                }
            }

            firestore.collection(VACCINATIONS_COLLECTION)
                .document(vaccination.id)
                .update(vaccination.toMap())
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

// Extension functions for data conversion
private fun com.google.firebase.firestore.DocumentSnapshot.toVaccinationDto(): VaccinationRecordDto {
    return VaccinationRecordDto(
        id = id,
        petId = getString("petId") ?: "",
        veterinarianId = getString("veterinarianId"),
        title = getString("title") ?: "",
        isCompleted = getBoolean("isCompleted") ?: false,
        date = getLong("date"),
        notes = getString("notes"),
        createdAt = getLong("createdAt") ?: 0L
    )
}

private fun VaccinationRecordDto.toMap(): Map<String, Any?> {
    return mapOf(
        "petId" to petId,
        "veterinarianId" to veterinarianId,
        "title" to title,
        "isCompleted" to isCompleted,
        "date" to date,
        "notes" to notes,
        "createdAt" to createdAt
    )
}

