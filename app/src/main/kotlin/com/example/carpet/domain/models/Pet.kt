package com.example.carpet.domain.models

/**
 * Represents a pet owned by a user in the CarPet application.
 */
data class Pet(
    val id: String,
    val ownerId: String,
    val name: String,
    val type: String, // e.g., "Dog", "Cat"
    val breed: String,
    val imageRes: Int
)
