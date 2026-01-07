package com.example.carpet.domain.models

/**
 * Represents a user in the CarPet application.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val points: Int,
    val profileImage: Int
)
