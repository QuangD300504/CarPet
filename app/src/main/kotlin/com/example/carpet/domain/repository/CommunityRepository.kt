package com.example.carpet.domain.repository

import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.PetEvent
import com.example.carpet.domain.models.Post
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getPosts(): Flow<List<Post>>
    fun getAdoptionPets(): Flow<List<Pet>>
    fun getEvents(): Flow<List<PetEvent>>
}
