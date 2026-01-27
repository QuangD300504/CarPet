package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User

interface UserRepository {
    /**
     * Get the current user profile from Firestore.
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Get pets owned by a specific user.
     */
    suspend fun getUserPets(userId: String): List<Pet>

    /**
     * Upload a new avatar image for the current user and update their profile.
     *
     * @return a [Result] containing the public HTTPS URL of the uploaded image.
     */
    suspend fun updateUserAvatar(imageBytes: ByteArray): Result<String>
}
