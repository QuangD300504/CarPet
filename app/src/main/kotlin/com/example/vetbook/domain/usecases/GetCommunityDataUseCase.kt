package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.PetEvent
import com.example.vetbook.domain.models.Post
import com.example.vetbook.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow

class GetCommunityDataUseCase(private val repository: CommunityRepository) {
    fun getPosts(): Flow<List<Post>> = repository.getPosts()
    fun getAdoptionPets(): Flow<List<Pet>> = repository.getAdoptionPets()
    fun getEvents(): Flow<List<PetEvent>> = repository.getEvents()
}
