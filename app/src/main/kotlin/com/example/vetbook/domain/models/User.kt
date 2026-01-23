package com.example.vetbook.domain.models

/**
 * Represents a user in the VetBook application.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val points: Int,
    val profileImage: Int
)
