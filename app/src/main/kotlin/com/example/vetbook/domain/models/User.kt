package com.example.vetbook.domain.models

/**
 * Represents a user in the VetBook application.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String = "",
    val points: Int,
    val profileImageUrl: String? = null,
    val profileImage: Int? = null
)
