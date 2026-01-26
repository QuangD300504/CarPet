package com.example.vetbook.data.repository

import com.example.vetbook.R
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.repository.UserRepository

class MockUserRepository : UserRepository {
    override suspend fun getCurrentUser(): User? {
        return User(
            id = "user_001",
            name = "John Pet Parent",
            email = "john@email.com",
            points = 230,
            profileImage = R.drawable.profile
        )
    }

    override suspend fun getUserPets(userId: String): List<Pet> {
        return listOf(
            Pet(
                id = "pet_001",
                ownerId = userId,
                name = "PiCi",
                type = "Dog",
                breed = "Golden Retriever",
                imageRes = R.drawable.dog_icon,
                age = "3 years 6 months",
                gender = "Male",
                weight = "28 kg",
                parasiticStatus = "Healthy",
                note = "Very energetic and loves to play. Needs regular exercise.",
                realImgUrl = "https://example.com/pici.jpg",
                vaccinations = listOf(
                    Vaccination(
                        id = "vac_001",
                        petId = "pet_001", // Foreign key relationship
                        veterinarianId = null,
                        title = "5-in-1",
                        isCompleted = true,
                        date = "2025-01-15"
                    ),
                    Vaccination(
                        id = "vac_002",
                        petId = "pet_001", // Foreign key relationship
                        veterinarianId = null,
                        title = "Rabies",
                        isCompleted = true,
                        date = "2025-01-15"
                    ),
                    Vaccination(
                        id = "vac_003",
                        petId = "pet_001", // Foreign key relationship
                        veterinarianId = null,
                        title = "DHPP Booster",
                        isCompleted = true,
                        date = "2024-12-20"
                    ),
                    Vaccination(
                        id = "vac_004",
                        petId = "pet_001", // Foreign key relationship
                        veterinarianId = null,
                        title = "Parasite Prevention",
                        isCompleted = false
                    )
                )
            ),
            Pet(
                id = "pet_002",
                ownerId = userId,
                name = "Bella",
                type = "Cat",
                breed = "Persian",
                imageRes = R.drawable.cat_icon,
                age = "2 years 3 months",
                gender = "Female",
                weight = "9.5 kg",
                parasiticStatus = "Healthy",
                note = "Calm and affectionate. Prefers indoor environment.",
                realImgUrl = "https://example.com/bella.jpg",
                vaccinations = emptyList()
            )
        )
    }
}
