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
}
