package com.example.vetbook.data.models

/**
 * Document in `posts/{postId}/comments` subcollection.
 */
data class CommentDto(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val likesCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isEdited: Boolean = false,
    val parentCommentId: String? = null
)


