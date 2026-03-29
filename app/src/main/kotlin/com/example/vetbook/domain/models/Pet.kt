package com.example.vetbook.domain.models

import androidx.annotation.Keep
import java.time.Instant

/**
 * Represents a pet in the VetBook application (either owned by a user or available for adoption).
 */
@Keep
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
    /** Used for WSAVA-aligned vaccine schedule generation. Null = age unknown. */
    val birthDate: Instant? = null,
    val vaccinations: List<Vaccination> = emptyList()
)
