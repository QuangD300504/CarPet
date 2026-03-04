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
    
    /**
     * Get posts by a specific author (user).
     * @param authorId The user ID of the post author
     * @return Flow of posts created by the user
     */
    suspend fun getPostsByAuthor(authorId: String): List<PostDto>
    
    /**
     * Create a new post with author relationship validation.
     * @param post The post data to create
     * @return Result containing the created post or error
     */
    suspend fun createPost(post: PostDto): Result<PostDto>

    fun observeAdoptionPets(): Flow<List<PetDto>>

    fun observeEvents(): Flow<List<PetEventDto>>
    
    /**
     * Get events organized by a specific user.
     * @param organizerId The user ID of the event organizer
     * @return List of events organized by the user
     */
    suspend fun getEventsByOrganizer(organizerId: String): List<PetEventDto>
    
    /**
     * Create a new event with organizer relationship validation.
     * @param event The event data to create
     * @return Result containing the created event or error
     */
    suspend fun createEvent(event: PetEventDto): Result<PetEventDto>

    /**
     * Toggle like on a post. Increments likesCount if not yet liked; decrements if already liked.
     * @param postId The post document ID
     * @param isCurrentlyLiked Whether the user has already liked this post
     */
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Result<Unit>
}


