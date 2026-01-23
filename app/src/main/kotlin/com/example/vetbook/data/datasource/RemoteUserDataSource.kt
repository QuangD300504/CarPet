package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.UserProfileDto

/**
 * Abstraction over the remote user profile source (e.g. Firestore).
 *
 * This keeps repositories independent from any Firebase-specific APIs.
 */
interface RemoteUserDataSource {

    /**
     * Fetch a user profile by uid. Returns null if the document does not exist.
     */
    suspend fun getUserProfile(uid: String): UserProfileDto?

    /**
     * Create or update the user profile document.
     */
    suspend fun setUserProfile(profile: UserProfileDto)

    /**
     * Partially update fields on the user profile document.
     */
    suspend fun updateUserProfileFields(
        uid: String,
        fields: Map<String, Any?>
    )
}


