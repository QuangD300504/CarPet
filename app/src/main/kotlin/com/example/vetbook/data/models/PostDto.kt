package com.example.vetbook.data.models

/**
 * Firestore document in `posts` collection.
 */
data class PostDto(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val imageUrls: List<String>? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isEdited: Boolean = false,
    val tags: List<String>? = null
)
