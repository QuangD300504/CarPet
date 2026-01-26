package com.example.vetbook.domain.models

/**
 * Represents a vaccination record for a pet.
 *
 * @param id Unique identifier for the vaccination record
 * @param petId Foreign key reference to Pet.id (which pet received this vaccination)
 * @param veterinarianId Optional foreign key reference to Veterinarian.id (who administered it)
 * @param title The name of the vaccination (e.g., "5-in-1", "Rabies")
 * @param isCompleted Whether the vaccination has been completed
 * @param date Optional vaccination date
 * @param notes Optional notes about the vaccination
 */
data class Vaccination(
    val id: String,
    val petId: String, // Foreign key to Pet
    val veterinarianId: String? = null, // Optional foreign key to Veterinarian
    val title: String,
    val isCompleted: Boolean,
    val date: String? = null,
    val notes: String? = null
)
