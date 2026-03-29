package com.example.vetbook.domain.models

import androidx.annotation.Keep

/**
 * Represents a user in the VetBook application.
 */
@Keep
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String = "",
    val points: Int,
    val profileImageUrl: String? = null,
    val profileImage: Int? = null,
    val isAdmin: Boolean = false
)
