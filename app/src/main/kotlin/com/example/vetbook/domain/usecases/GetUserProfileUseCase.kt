package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User
import com.example.vetbook.domain.repository.UserRepository

/**
 * Use case for retrieving user profile data including user info and their pets.
 */
class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Executes the use case to get user profile data.
     * @return Pair of User and their pets list, or null if user not found
     */
    suspend operator fun invoke(): Pair<User, List<Pet>>? {
        val user = userRepository.getCurrentUser() ?: return null
        val pets = userRepository.getUserPets(user.id)
        return Pair(user, pets)
    }
}
