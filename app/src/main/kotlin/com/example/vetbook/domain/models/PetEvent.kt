package com.example.vetbook.domain.models

/**
 * Represents a pet-related event in the VetBook application.
 * 
 * @param id Unique identifier for the event
 * @param organizerId Foreign key reference to User.id (who organized the event)
 * @param title Event title
 * @param date Event date
 * @param location Event location
 * @param imageUrl Optional event image URL
 * @param iconRes Optional icon resource ID
 */
data class PetEvent(
    val id: String,
    val organizerId: String, // Foreign key to User
    val title: String,
    val date: String,
    val location: String,
    val imageUrl: String?,
    val iconRes: Int? = null
)
