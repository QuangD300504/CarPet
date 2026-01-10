package com.example.vetbook.data.repository

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.PetEvent
import com.example.vetbook.domain.models.Post
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockCommunityRepository : CommunityRepository {
    override fun getPosts(): Flow<List<Post>> = flowOf(
        listOf(
            Post(
                id = "1",
                authorName = "Sarah M.",
                authorAvatarUrl = null,
                timestamp = "2 hours ago",
                content = "Just adopted the sweetest chi-huahua retriever puppy! Any tips for first-time dog owners? \uD83D\uDC15",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1",
                likesCount = 42,
                commentsCount = 15
            ),
            Post(
                id = "2",
                authorName = "Mike T.",
                authorAvatarUrl = null,
                timestamp = "2 hours ago",
                content = "Does anyone know a good vet near downtown? My cat needs a checkup \uD83D\uDE3A",
                imageUrl = null,
                likesCount = 28,
                commentsCount = 9
            )
        )
    )

    override fun getAdoptionPets(): Flow<List<Pet>> = flowOf(
        listOf(
            Pet(
                id = "adopt_001",
                name = "Luna",
                type = "Dog",
                breed = "Labrador",
                age = "2 years",
                gender = "Female",
                weight = "22 kg",
                parasiticStatus = "Healthy",
                note = "Luna is very friendly and well-trained. She loves long walks and playing fetch.",
                vaccinations = listOf(
                    Vaccination("v1", "Rabies", true, "2024-10-12"),
                    Vaccination("v2", "DHPP", true, "2024-11-05")
                )
            ),
            Pet(
                id = "adopt_002",
                name = "Oliver",
                type = "Cat",
                breed = "British Shorthair",
                age = "1 year",
                gender = "Male",
                weight = "4.5 kg",
                parasiticStatus = "Healthy",
                note = "Oliver is a quiet and independent cat. He enjoys being petted but also likes his space.",
                vaccinations = listOf(
                    Vaccination("v3", "FVRCP", true, "2025-01-20"),
                    Vaccination("v4", "Rabies", false)
                )
            )
        )
    )

    override fun getEvents(): Flow<List<PetEvent>> = flowOf(
        listOf(
            PetEvent(
                id = "1",
                title = "Pet Adoption Fair",
                date = "Saturday, Nov 15",
                location = "Central Park, 10:00 AM",
                imageUrl = null
            )
        )
    )
}
