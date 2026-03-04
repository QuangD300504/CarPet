package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.PetEvent
import com.example.vetbook.domain.models.Post
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getPosts(): Flow<List<Post>>
    fun getAdoptionPets(): Flow<List<Pet>>
    fun getEvents(): Flow<List<PetEvent>>
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Result<Unit>
}
