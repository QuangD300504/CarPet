package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.repository.UserRepository

/**
 * Use case for retrieving pet profile from the current user's pets only.
 */
class GetPetProfileUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Executes the use case to find a pet by ID.
     * @param petId The ID of the pet to find
     * @return The found Pet, or null if not found
     */
    suspend operator fun invoke(petId: String): Pet? {
        val currentUser = userRepository.getCurrentUser() ?: return null
        return userRepository.getUserPets(currentUser.id).find { it.id == petId }
    }
}

