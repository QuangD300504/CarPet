package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.PetDto
import com.example.vetbook.data.models.PetEventDto
import com.example.vetbook.data.models.PostDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote source for community feed, adoption pets, and events.
 */
interface RemoteCommunityDataSource {

    fun observePosts(): Flow<List<PostDto>>

    fun observeAdoptionPets(): Flow<List<PetDto>>

    fun observeEvents(): Flow<List<PetEventDto>>
}


