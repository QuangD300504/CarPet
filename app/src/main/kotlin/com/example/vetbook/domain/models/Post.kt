package com.example.vetbook.domain.models

/**
 * Represents a community post in the VetBook application.
 * 
 * @param id Unique identifier for the post
 * @param authorId Foreign key reference to User.id (who created the post)
 * @param authorName Denormalized author name for display
 * @param authorAvatarUrl Denormalized author avatar URL
 * @param timestamp Post creation timestamp
 * @param content Post content/text
 * @param imageUrl Optional image URL attached to post
 * @param likesCount Number of likes
 * @param commentsCount Number of comments
 */
data class Post(
    val id: String,
    val authorId: String, // Foreign key to User
    val authorName: String,
    val authorAvatarUrl: String?,
    val timestamp: String,
    val content: String,
    val imageUrl: String?,
    val likesCount: Int,
    val commentsCount: Int
)
