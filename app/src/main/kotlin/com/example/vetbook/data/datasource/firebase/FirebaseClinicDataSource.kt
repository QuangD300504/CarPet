package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteClinicDataSource
import com.example.vetbook.data.models.ClinicDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val CLINICS_COLLECTION = "clinics"

class FirebaseClinicDataSource(
    private val firestore: FirebaseFirestore
) : RemoteClinicDataSource {

    override fun observeClinics(): Flow<List<ClinicDto>> = callbackFlow {
        val reg: ListenerRegistration = firestore
            .collection(CLINICS_COLLECTION)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        doc.toClinicDto()
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(items)
            }

        awaitClose { reg.remove() }
    }

    override suspend fun getClinicById(id: String): ClinicDto? {
        return try {
            val snapshot = firestore
                .collection(CLINICS_COLLECTION)
                .document(id)
                .get()
                .await()

            if (!snapshot.exists()) return null
            snapshot.toClinicDto()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun createClinic(clinic: ClinicDto): Result<ClinicDto> {
        return try {
            // Clinics use auto-generated document IDs
            val docRef = firestore.collection(CLINICS_COLLECTION).document()

            val now = System.currentTimeMillis()
            val withTimestamps = clinic.copy(
                id = docRef.id,
                createdAt = clinic.createdAt ?: now,
                updatedAt = now
            )

            docRef.set(withTimestamps.toMap()).await()
            Result.success(withTimestamps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateClinic(id: String, fields: Map<String, Any?>) {
        if (fields.isEmpty()) return

        val updateFields = fields.toMutableMap()
        updateFields["updatedAt"] = System.currentTimeMillis()

        firestore
            .collection(CLINICS_COLLECTION)
            .document(id)
            .update(updateFields)
            .await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toClinicDto(): ClinicDto {
        val addressMap = get("address") as? Map<*, *>
        val coordsMap = get("coordinates") as? Map<*, *>

        return ClinicDto(
            id = id,
            name = getString("name") ?: "",
            address = ClinicDto.Address(
                street = addressMap?.get("street") as? String ?: "",
                city = addressMap?.get("city") as? String ?: "",
                state = addressMap?.get("state") as? String ?: "",
                zipCode = addressMap?.get("zipCode") as? String ?: ""
            ),
            coordinates = coordsMap?.let {
                ClinicDto.Coordinates(
                    latitude = (it["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (it["longitude"] as? Number)?.toDouble() ?: 0.0
                )
            },
            imageUrl = getString("imageUrl"),
            phone = getString("phone") ?: "",
            email = getString("email") ?: "",
            website = getString("website"),
            operatingHours = (get("operatingHours") as? Map<*, *>)
                ?.mapNotNull { (k, v) ->
                    val key = k as? String ?: return@mapNotNull null
                    val value = v as? String ?: return@mapNotNull null
                    key to value
                }
                ?.toMap()
                ?: emptyMap(),
            createdAt = getLong("createdAt"),
            updatedAt = getLong("updatedAt")
        )
    }

    private fun ClinicDto.toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "address" to hashMapOf(
                "street" to address.street,
                "city" to address.city,
                "state" to address.state,
                "zipCode" to address.zipCode
            ),
            "coordinates" to coordinates?.let {
                hashMapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude
                )
            },
            "imageUrl" to imageUrl,
            "phone" to phone,
            "email" to email,
            "website" to website,
            "operatingHours" to operatingHours,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
