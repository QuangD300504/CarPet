package com.example.vetbook.domain.models

/**
 * Represents a pet in the CarPet application (either owned by a user or available for adoption).
 */
data class Pet(
    val id: String,
    val ownerId: String? = null, // Null if the pet is for adoption
    val name: String,
    val type: String, // e.g., "Dog", "Cat"
    val breed: String,
    val imageRes: Int? = null,
    val age: String = "", // e.g., "2 years 3 months"
    val gender: String = "", // e.g., "Male", "Female"
    val weight: String = "", // e.g., "9.5 kg"
    val parasiticStatus: String = "", // e.g., "Healthy"
    val note: String = "",
    val realImgUrl: String? = null,
    val vaccinations: List<Vaccination> = emptyList()
)
