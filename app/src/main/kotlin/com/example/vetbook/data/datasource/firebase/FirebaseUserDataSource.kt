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
            profileImageUrl = snapshot.getString("profileImageUrl"),
            createdAt = snapshot.getLong("createdAt") ?: 0L,
            updatedAt = snapshot.getLong("updatedAt"),
            isEmailVerified = snapshot.getBoolean("isEmailVerified") ?: false,
            lastLogin = snapshot.getLong("lastLogin")
        )
    }

    override suspend fun setUserProfile(profile: UserProfileDto) {
        val data = hashMapOf(
            "uid" to profile.uid,
            "fullName" to profile.fullName,
            "email" to profile.email,
            "phone" to profile.phone,
            "profileImageUrl" to profile.profileImageUrl,
            "createdAt" to profile.createdAt,
            "updatedAt" to profile.updatedAt,
            "isEmailVerified" to profile.isEmailVerified,
            "lastLogin" to profile.lastLogin
        ).filterValues { it != null }

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


