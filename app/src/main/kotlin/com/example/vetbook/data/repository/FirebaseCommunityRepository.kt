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
 * Firebase implementation of CommunityRepository.
 * Maps DTOs from RemoteCommunityDataSource to domain models.
 */
class FirebaseCommunityRepository(
    private val remoteDataSource: RemoteCommunityDataSource
) : CommunityRepository {

    override fun getPosts(): Flow<List<Post>> =
        remoteDataSource.observePosts().map { dtos ->
            dtos.map { it.toDomain() }
        }

    override fun getAdoptionPets(): Flow<List<Pet>> =
        remoteDataSource.observeAdoptionPets().map { dtos ->
            dtos.map { it.toDomain() }
        }

    override fun getEvents(): Flow<List<PetEvent>> =
        remoteDataSource.observeEvents().map { dtos ->
            dtos.map { it.toDomain() }
        }

    override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Result<Unit> =
        remoteDataSource.toggleLike(postId, isCurrentlyLiked)
}
