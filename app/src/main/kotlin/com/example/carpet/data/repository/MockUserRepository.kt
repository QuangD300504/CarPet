package com.example.carpet.data.repository

import com.example.carpet.R
import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.User
import com.example.carpet.domain.models.Vaccination
import com.example.carpet.domain.repository.UserRepository

class MockUserRepository : UserRepository {
    override fun getCurrentUser(): User? {
        return User(
            id = "user_001",
            name = "John Pet Parent",
            email = "john@email.com",
            points = 230,
            profileImage = R.drawable.profile // Using placeholder drawable
        )
    }

    override fun getUserPets(userId: String): List<Pet> {
        return listOf(
            Pet(
                id = "pet_001",
                ownerId = userId,
                name = "PiCi",
                type = "Dog",
                breed = "Golden Retriever",
                imageRes = R.drawable.dog_icon, // Using placeholder drawable
                age = "3 years 6 months",
                gender = "Male",
                weight = "28 kg",
                parasiticStatus = "Healthy",
                note = "Very energetic and loves to play. Needs regular exercise.",
                realImgUrl = "https://example.com/pici.jpg",
                vaccinations = listOf(
                    Vaccination(
                        id = "vac_001",
                        title = "5-in-1",
                        isCompleted = true,
                        date = "2025-01-15"
                    ),
                    Vaccination(
                        id = "vac_002",
                        title = "Rabies",
                        isCompleted = true,
                        date = "2025-01-15"
                    ),
                    Vaccination(
                        id = "vac_003",
                        title = "DHPP Booster",
                        isCompleted = true,
                        date = "2024-12-20"
                    ),
                    Vaccination(
                        id = "vac_004",
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
                imageRes = R.drawable.cat_icon, // Using placeholder drawable
                age = "2 years 3 months",
                gender = "Female",
                weight = "9.5 kg",
                parasiticStatus = "Healthy",
                note = "Calm and affectionate. Prefers indoor environment. Regular checkups recommended.",
                realImgUrl = "https://example.com/bella.jpg",
                vaccinations = listOf(
                    Vaccination(
                        id = "vac_005",
                        title = "FVRCP",
                        isCompleted = true,
                        date = "2025-02-10"
                    ),
                    Vaccination(
                        id = "vac_006",
                        title = "Rabies",
                        isCompleted = true,
                        date = "2025-02-10"
                    ),
                    Vaccination(
                        id = "vac_007",
                        title = "Feline Leukemia",
                        isCompleted = true,
                        date = "2024-11-05"
                    ),
                    Vaccination(
                        id = "vac_008",
                        title = "FVRCP Booster",
                        isCompleted = false
                    )
                )
            )
        )
    }
}
