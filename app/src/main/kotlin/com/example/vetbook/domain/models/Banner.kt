package com.example.vetbook.domain.models

data class Banner(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val targetUrl: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L
)
