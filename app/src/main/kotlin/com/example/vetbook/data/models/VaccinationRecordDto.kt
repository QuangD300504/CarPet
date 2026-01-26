package com.example.vetbook.data.models

/**
 * Firestore document representing a vaccination record.
 * Can be stored as subcollection `pets/{petId}/vaccinations` or in `vaccinations` collection.
 * 
 * Relationships:
 * - petId: Foreign key to Pet (required)
 * - veterinarianId: Foreign key to Veterinarian (optional)
 */
data class VaccinationRecordDto(
    val id: String = "",
    val petId: String = "", // Foreign key to Pet (required)
    val veterinarianId: String? = null, // Foreign key to Veterinarian (optional)
    val title: String = "",
    val isCompleted: Boolean = false,
    val date: Long? = null,
    val notes: String? = null,
    val createdAt: Long = 0L
)


