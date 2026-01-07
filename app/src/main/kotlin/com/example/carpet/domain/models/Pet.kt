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
    val imageRes: Int,
    val age: String = "", // e.g., "2 years 3 months"
    val gender: String = "", // e.g., "Male", "Female"
    val weight: String = "", // e.g., "9.5 kg"
    val parasiticStatus: String = "", // e.g., "Buồn", "Healthy"
    val note: String = "",
    val realImgUrl: String? = null,
    val vaccinations: List<Vaccination> = emptyList()
)
