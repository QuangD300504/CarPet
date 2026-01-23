package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val PETS_COLLECTION = "pets"

class FirebasePetDataSource(
    private val firestore: FirebaseFirestore
) : RemotePetDataSource {

    override suspend fun getUserPets(ownerId: String): List<PetDto> {
        return try {
            val snapshot = firestore
                .collection(PETS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()

            snapshot.documents.map { doc ->
                PetDto(
                    id = doc.id,
                    ownerId = doc.getString("ownerId"),
                    name = doc.getString("name") ?: "",
                    type = doc.getString("type") ?: "",
                    breed = doc.getString("breed") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    age = doc.getString("age") ?: "",
                    gender = doc.getString("gender") ?: "",
                    weight = doc.getString("weight") ?: "",
                    parasiticStatus = doc.getString("parasiticStatus") ?: "",
                    note = doc.getString("note") ?: "",
                    isForAdoption = doc.getBoolean("isForAdoption") ?: false
                )
            }
        } catch (e: Exception) {
            // Return empty list on error (network, permission, etc.)
            emptyList()
        }
    }

    override suspend fun getAdoptionPets(): List<PetDto> {
        return try {
            val snapshot = firestore
                .collection(PETS_COLLECTION)
                .whereEqualTo("isForAdoption", true)
                .get()
                .await()

            snapshot.documents.map { doc ->
                PetDto(
                    id = doc.id,
                    ownerId = doc.getString("ownerId"),
                    name = doc.getString("name") ?: "",
                    type = doc.getString("type") ?: "",
                    breed = doc.getString("breed") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    age = doc.getString("age") ?: "",
                    gender = doc.getString("gender") ?: "",
                    weight = doc.getString("weight") ?: "",
                    parasiticStatus = doc.getString("parasiticStatus") ?: "",
                    note = doc.getString("note") ?: "",
                    isForAdoption = doc.getBoolean("isForAdoption") ?: false
                )
            }
        } catch (e: Exception) {
            // Return empty list on error
            emptyList()
        }
    }
}


