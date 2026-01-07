package com.example.carpet.data.repository

import com.example.carpet.R
import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.User
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
                imageRes = R.drawable.dog_icon // Using placeholder drawable
            ),
            Pet(
                id = "pet_002",
                ownerId = userId,
                name = "Bella",
                type = "Cat",
                breed = "Persian Cat",
                imageRes = R.drawable.cat_icon // Using placeholder drawable
            )
        )
    }
}
