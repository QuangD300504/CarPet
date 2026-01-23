package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteCommunityDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.PetEvent
import com.example.vetbook.domain.models.Post
import com.example.vetbook.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Firebase-backed implementation of CommunityRepository.
 *
 * Currently not wired into DI by default; mocks are still used for UI.
 * To switch, bind this implementation in RepositoryModule.
 */
class FirebaseCommunityRepository(
    private val remoteCommunityDataSource: RemoteCommunityDataSource
) : CommunityRepository {

    override fun getPosts(): Flow<List<Post>> =
        remoteCommunityDataSource.observePosts()
            .map { posts -> posts.map { it.toDomain() } }

    override fun getAdoptionPets(): Flow<List<Pet>> =
        remoteCommunityDataSource.observeAdoptionPets()
            .map { pets -> pets.map { it.toDomain() } }

    override fun getEvents(): Flow<List<PetEvent>> =
        remoteCommunityDataSource.observeEvents()
            .map { events -> events.map { it.toDomain() } }
}


