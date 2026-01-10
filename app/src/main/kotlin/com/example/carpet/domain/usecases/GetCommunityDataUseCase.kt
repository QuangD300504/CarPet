package com.example.carpet.domain.usecases

import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.PetEvent
import com.example.carpet.domain.models.Post
import com.example.carpet.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow

class GetCommunityDataUseCase(private val repository: CommunityRepository) {
    fun getPosts(): Flow<List<Post>> = repository.getPosts()
    fun getAdoptionPets(): Flow<List<Pet>> = repository.getAdoptionPets()
    fun getEvents(): Flow<List<PetEvent>> = repository.getEvents()
}
