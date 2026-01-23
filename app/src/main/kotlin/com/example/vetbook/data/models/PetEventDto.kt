package com.example.vetbook.data.models

/**
 * Firestore document in `petEvents` collection.
 */
data class PetEventDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val location: String = "",
    val imageUrl: String? = null,
    val organizerId: String = "",
    val organizerName: String = "",
    val eventType: String = "",
    val maxParticipants: Int? = null,
    val currentParticipants: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null
)
