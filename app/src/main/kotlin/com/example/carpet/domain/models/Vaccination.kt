package com.example.carpet.domain.models

/**
 * Represents a vaccination record for a pet.
 *
 * @param id Unique identifier for the vaccination record
 * @param title The name of the vaccination (e.g., "5-in-1", "Rabies")
 * @param isCompleted Whether the vaccination has been completed
 * @param date Optional vaccination date
 */
data class Vaccination(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val date: String? = null
)
