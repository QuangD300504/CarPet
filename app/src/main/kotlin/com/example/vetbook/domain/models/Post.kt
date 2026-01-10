package com.example.vetbook.domain.models

data class Post(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val timestamp: String,
    val content: String,
    val imageUrl: String?,
    val likesCount: Int,
    val commentsCount: Int
)
