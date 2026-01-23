package com.example.vetbook.data.models

/**
 * Document in `posts/{postId}/likes` subcollection.
 * Document ID is typically the userId.
 */
data class LikeDto(
    val userId: String = "",
    val createdAt: Long = 0L
)


