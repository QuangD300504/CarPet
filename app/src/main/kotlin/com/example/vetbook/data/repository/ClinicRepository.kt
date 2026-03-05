package com.example.vetbook.data.repository

import com.example.vetbook.domain.models.Clinic
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClinicRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getClinicById(clinicId: String): Clinic? {
        if (clinicId.isBlank()) return null
        return try {
            val snap = firestore.collection("clinics").document(clinicId).get().await()
            if (!snap.exists()) return null
            Clinic(
                id        = snap.id,
                name      = snap.getString("name") ?: "",
                address   = snap.getString("address") ?: "",
                latitude  = snap.getDouble("latitude") ?: 0.0,
                longitude = snap.getDouble("longitude") ?: 0.0,
                phone     = snap.getString("phone") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
