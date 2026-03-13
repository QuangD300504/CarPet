package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteAccommodationDataSource
import com.example.vetbook.data.models.AccommodationDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val ACCOMMODATIONS_COLLECTION = "accommodations"

class FirebaseAccommodationDataSource(
    private val firestore: FirebaseFirestore
) : RemoteAccommodationDataSource {

    override suspend fun getAccommodations(): List<AccommodationDto> {
        return try {
            firestore.collection(ACCOMMODATIONS_COLLECTION)
                .get()
                .await()
                .documents
                .map { doc ->
                    doc.toAccommodationDto()
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAccommodationById(id: String): AccommodationDto? {
        return try {
            firestore.collection(ACCOMMODATIONS_COLLECTION)
                .document(id)
                .get()
                .await()
                .takeIf { it.exists() }
                ?.toAccommodationDto()
        } catch (e: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAccommodationDto(): AccommodationDto {
        return AccommodationDto(
            id = id,
            name = getString("name") ?: "",
            category = getString("category") ?: "",
            location = getString("location") ?: "",
            district = getString("district") ?: "",
            rating = (getDouble("rating") ?: 0.0).toFloat(),
            reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
            price = getDouble("price") ?: 0.0,
            priceUnit = getString("priceUnit") ?: "USD",
            imageUrl = getString("imageUrl"),
            description = getString("description") ?: "",
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            isPopular = getBoolean("isPopular") ?: false,
            createdAt = getLong("createdAt"),
            updatedAt = getLong("updatedAt")
        )
    }
}
