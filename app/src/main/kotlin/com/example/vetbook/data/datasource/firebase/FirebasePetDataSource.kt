package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

private const val PETS_COLLECTION = "pets"
private const val USERS_COLLECTION = "users"

class FirebasePetDataSource(
    private val firestore: FirebaseFirestore
) : RemotePetDataSource {

    override suspend fun getUserPets(ownerId: String): List<PetDto> {
        return try {
            firestore.collection(PETS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
                .documents
                .map { it.toPetDto() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAdoptionPets(): List<PetDto> {
        return try {
            firestore.collection(PETS_COLLECTION)
                .whereEqualTo("isForAdoption", true)
                .get()
                .await()
                .documents
                .map { it.toPetDto() }
        } catch (e: Exception) {
            emptyList()
        }
    }


    override suspend fun createPet(pet: PetDto): Result<PetDto> {
        return try {
            // Validate owner exists if ownerId is provided (foreign key validation)
            if (pet.ownerId != null && pet.ownerId.isNotBlank()) {
                val ownerDoc = firestore.collection(USERS_COLLECTION)
                    .document(pet.ownerId)
                    .get()
                    .await()
                
                if (!ownerDoc.exists()) {
                    return Result.failure(Exception("Owner user not found: ${pet.ownerId}"))
                }
            }

            // Create pet document with timestamp
            val docRef = if (pet.id.isBlank()) {
                firestore.collection(PETS_COLLECTION).document()
            } else {
                firestore.collection(PETS_COLLECTION).document(pet.id)
            }

            val now = System.currentTimeMillis()
            val petData = pet.copy(
                id = docRef.id,
                createdAt = now,
                updatedAt = now
            )

            docRef.set(petData.toMap()).await()
            Result.success(petData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePet(pet: PetDto): Result<Unit> {
        return try {
            if (pet.id.isBlank()) {
                return Result.failure(Exception("Pet ID is required for update"))
            }

            // Validate owner exists if ownerId is being changed
            if (pet.ownerId != null && pet.ownerId.isNotBlank()) {
                val ownerDoc = firestore.collection(USERS_COLLECTION)
                    .document(pet.ownerId)
                    .get()
                    .await()
                
                if (!ownerDoc.exists()) {
                    return Result.failure(Exception("Owner user not found: ${pet.ownerId}"))
                }
            }

            val petData = pet.copy(
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(PETS_COLLECTION)
                .document(pet.id)
                .update(petData.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePet(petId: String): Result<Unit> {
        return try {
            if (petId.isBlank()) {
                return Result.failure(Exception("Pet ID is required"))
            }

            firestore.collection(PETS_COLLECTION)
                .document(petId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPetById(petId: String): Result<PetDto> {
        return try {
            if (petId.isBlank()) {
                return Result.failure(Exception("Pet ID is required"))
            }

            val doc = firestore.collection(PETS_COLLECTION)
                .document(petId)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("Pet not found: $petId"))
            }

            val petDto = doc.toPetDto()
            Result.success(petDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert Firestore document to PetDto
private fun com.google.firebase.firestore.DocumentSnapshot.toPetDto(): PetDto {
    return PetDto(
        id = id,
        ownerId = getString("ownerId"),
        name = getString("name") ?: "",
        type = getString("type") ?: "",
        breed = getString("breed") ?: "",
        imageUrl = getString("imageUrl"),
        age = getString("age") ?: "",
        gender = getString("gender") ?: "",
        weight = getString("weight") ?: "",
        parasiticStatus = getString("parasiticStatus") ?: "",
        note = getString("note") ?: "",
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt"),
        isForAdoption = getBoolean("isForAdoption") ?: false
    )
}

// Extension function to convert PetDto to Firestore map
private fun PetDto.toMap(): Map<String, Any?> {
    return mapOf(
        "ownerId" to ownerId,
        "name" to name,
        "type" to type,
        "breed" to breed,
        "imageUrl" to imageUrl,
        "age" to age,
        "gender" to gender,
        "weight" to weight,
        "parasiticStatus" to parasiticStatus,
        "note" to note,
        "createdAt" to (createdAt ?: System.currentTimeMillis()),
        "updatedAt" to (updatedAt ?: System.currentTimeMillis()),
        "isForAdoption" to isForAdoption
    )
}


