package com.example.vetbook.data.models

import androidx.annotation.Keep

/**
 * Firestore representation of a user document in `users` collection.
 *
 * Mirrors the structure defined in the Firestore database design plan.
 */
@Keep
data class UserProfileDto(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isEmailVerified: Boolean = false,
    val lastLogin: Long? = null,
    val points: Int = 0,
    val address: Address? = null,
    val preferences: Preferences? = null,
    val isAdmin: Boolean = false
) {
    data class Address(
        val street: String = "",
        val city: String = "",
        val state: String = "",
        val zipCode: String = "",
        val country: String = ""
    )

    data class Preferences(
        val notificationsEnabled: Boolean = true,
        val language: String = "en"
    )
}
