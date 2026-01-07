package com.example.carpet.domain.repository

import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.User

interface UserRepository {
    /**
     * Get the current user profile.
     */
    fun getCurrentUser(): User?
    
    /**
     * Get pets owned by a specific user.
     */
    fun getUserPets(userId: String): List<Pet>
}
