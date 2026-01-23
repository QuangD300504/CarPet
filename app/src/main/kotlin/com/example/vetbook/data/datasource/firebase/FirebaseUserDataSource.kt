package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.models.UserProfileDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"

class FirebaseUserDataSource(
    private val firestore: FirebaseFirestore
) : RemoteUserDataSource {

    override suspend fun getUserProfile(uid: String): UserProfileDto? {
        val snapshot = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) return null

        return UserProfileDto(
            uid = snapshot.getString("uid") ?: uid,
            fullName = snapshot.getString("fullName") ?: "",
            email = snapshot.getString("email") ?: "",
            phone = snapshot.getString("phone") ?: "",
            createdAt = snapshot.getLong("createdAt") ?: 0L,
            isEmailVerified = snapshot.getBoolean("isEmailVerified") ?: false
        )
    }

    override suspend fun setUserProfile(profile: UserProfileDto) {
        val data = hashMapOf(
            "uid" to profile.uid,
            "fullName" to profile.fullName,
            "email" to profile.email,
            "phone" to profile.phone,
            "createdAt" to profile.createdAt,
            "isEmailVerified" to profile.isEmailVerified
        )

        firestore
            .collection(USERS_COLLECTION)
            .document(profile.uid)
            .set(data)
            .await()
    }

    override suspend fun updateUserProfileFields(
        uid: String,
        fields: Map<String, Any?>
    ) {
        if (fields.isEmpty()) return

        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .update(fields)
            .await()
    }
}


