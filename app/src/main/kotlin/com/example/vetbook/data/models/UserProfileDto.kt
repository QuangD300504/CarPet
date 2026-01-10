package com.example.vetbook.data.models

data class UserProfileDto(
    val uid: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val createdAt: Long,
    val isEmailVerified: Boolean
)
