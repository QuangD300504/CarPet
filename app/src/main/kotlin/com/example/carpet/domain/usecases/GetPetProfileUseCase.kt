package com.example.carpet.domain.usecases

import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.repository.CommunityRepository
import com.example.carpet.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

/**
 * Use case for retrieving pet profile by searching in user's pets first,
 * then in community adoption pets if not found.
 */
class GetPetProfileUseCase(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository
) {
    /**
     * Executes the use case to find a pet by ID.
     * @param petId The ID of the pet to find
     * @return The found Pet, or null if not found
     */
    suspend operator fun invoke(petId: String): Pet? {
        // 1. First, search in User's pets
        val currentUser = userRepository.getCurrentUser()
        if (currentUser != null) {
            val userPet = userRepository.getUserPets(currentUser.id).find { it.id == petId }
            if (userPet != null) return userPet
        }

        // 2. If not found, search in Community adoption pets
        val adoptionPets = communityRepository.getAdoptionPets().first()
        return adoptionPets.find { it.id == petId }
    }
}

