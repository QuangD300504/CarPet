package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteVeterinarianDataSource
import com.example.vetbook.data.models.VeterinarianDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val VETERINARIANS_COLLECTION = "veterinarians"

/**
 * Firebase Firestore implementation of RemoteVeterinarianDataSource.
 * Manages veterinarian data in the `veterinarians` collection.
 */
class FirebaseVeterinarianDataSource(
    private val firestore: FirebaseFirestore
) : RemoteVeterinarianDataSource {

    override fun observeVeterinarians(): Flow<List<VeterinarianDto>> = callbackFlow {
        val registration = firestore
            .collection(VETERINARIANS_COLLECTION)
            .whereEqualTo("isActive", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't close the flow
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener

                val veterinarians = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toVeterinarianDto()
                    } catch (e: Exception) {
                        null // Skip malformed documents
                    }
                }
                trySend(veterinarians).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getVeterinarianById(id: String): VeterinarianDto? {
        return try {
            val snapshot = firestore
                .collection(VETERINARIANS_COLLECTION)
                .document(id)
                .get()
                .await()

            if (!snapshot.exists()) return null
            snapshot.toVeterinarianDto()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createVeterinarian(veterinarian: VeterinarianDto): Result<VeterinarianDto> {
        return try {
            val docRef = if (veterinarian.id.isBlank()) {
                firestore.collection(VETERINARIANS_COLLECTION).document()
            } else {
                firestore.collection(VETERINARIANS_COLLECTION).document(veterinarian.id)
            }

            val now = System.currentTimeMillis()
            val veterinarianWithTimestamps = veterinarian.copy(
                id = docRef.id,
                createdAt = veterinarian.createdAt ?: now,
                updatedAt = now
            )

            docRef.set(veterinarianWithTimestamps.toMap()).await()
            Result.success(veterinarianWithTimestamps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVeterinarian(id: String, fields: Map<String, Any?>) {
        try {
            if (fields.isEmpty()) return

            val updateFields = fields.toMutableMap()
            updateFields["updatedAt"] = System.currentTimeMillis()

            firestore
                .collection(VETERINARIANS_COLLECTION)
                .document(id)
                .update(updateFields)
                .await()
        } catch (e: Exception) {
            // Handle error appropriately
        }
    }

    /**
     * Extension function to convert Firestore DocumentSnapshot to VeterinarianDto.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toVeterinarianDto(): VeterinarianDto {
        return VeterinarianDto(
            id = id,
            name = getString("name") ?: "",
            specialty = getString("specialty") ?: "",
            experience = getString("experience") ?: "",
            rating = getDouble("rating") ?: 0.0,
            reviewsCount = getLong("reviewsCount")?.toInt() ?: 0,
            initials = getString("initials") ?: "",
            bio = getString("bio") ?: "",
            imageUrl = getString("imageUrl"),
            email = getString("email") ?: "",
            phone = getString("phone") ?: "",
            clinic = getClinic(),
            availability = getAvailability(),
            isActive = getBoolean("isActive") ?: true,
            createdAt = getLong("createdAt"),
            updatedAt = getLong("updatedAt")
        )
    }

    /**
     * Extract nested Clinic object from Firestore document.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.getClinic(): VeterinarianDto.Clinic? {
        val clinicMap = get("clinic") as? Map<*, *> ?: return null
        
        return try {
            val addressMap = clinicMap["address"] as? Map<*, *>
            val coordinatesMap = clinicMap["coordinates"] as? Map<*, *>
            
            VeterinarianDto.Clinic(
                name = clinicMap["name"] as? String ?: "",
                address = VeterinarianDto.Address(
                    street = addressMap?.get("street") as? String ?: "",
                    city = addressMap?.get("city") as? String ?: "",
                    state = addressMap?.get("state") as? String ?: "",
                    zipCode = addressMap?.get("zipCode") as? String ?: ""
                ),
                coordinates = if (coordinatesMap != null) {
                    VeterinarianDto.Coordinates(
                        latitude = coordinatesMap["latitude"] as? Double ?: 0.0,
                        longitude = coordinatesMap["longitude"] as? Double ?: 0.0
                    )
                } else null
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract nested Availability object from Firestore document.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.getAvailability(): VeterinarianDto.Availability? {
        val availabilityMap = get("availability") as? Map<*, *> ?: return null
        
        return try {
            val hoursMap = availabilityMap["hours"] as? Map<*, *>
            
            VeterinarianDto.Availability(
                days = (availabilityMap["days"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                hours = VeterinarianDto.Hours(
                    start = hoursMap?.get("start") as? String ?: "",
                    end = hoursMap?.get("end") as? String ?: ""
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert VeterinarianDto to Firestore-compatible Map.
     */
    private fun VeterinarianDto.toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "specialty" to specialty,
            "experience" to experience,
            "rating" to rating,
            "reviewsCount" to reviewsCount,
            "initials" to initials,
            "bio" to bio,
            "imageUrl" to imageUrl,
            "email" to email,
            "phone" to phone,
            "clinic" to clinic?.let { clinic ->
                hashMapOf(
                    "name" to clinic.name,
                    "address" to hashMapOf(
                        "street" to clinic.address.street,
                        "city" to clinic.address.city,
                        "state" to clinic.address.state,
                        "zipCode" to clinic.address.zipCode
                    ),
                    "coordinates" to clinic.coordinates?.let {
                        hashMapOf(
                            "latitude" to it.latitude,
                            "longitude" to it.longitude
                        )
                    }
                )
            },
            "availability" to availability?.let { avail ->
                hashMapOf(
                    "days" to avail.days,
                    "hours" to hashMapOf(
                        "start" to avail.hours.start,
                        "end" to avail.hours.end
                    )
                )
            },
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
