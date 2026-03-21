package com.example.vetbook.data.models

/**
 * Firestore representation of a pet document.
 *
 * Fields are nullable / have defaults to keep reads resilient to
 * missing data while the project is evolving.
 */
data class PetDto(
    val id: String = "",
    val ownerId: String? = null,
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val imageUrl: String? = null,
    val age: String = "",
    val gender: String = "",
    val weight: String = "",
    val parasiticStatus: String = "",
    val note: String = "",
    val birthDate: Long? = null, // epoch millis — used for vaccine schedule generation
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val isForAdoption: Boolean = false,
    val adoptionDetails: AdoptionDetails? = null
) {
    data class AdoptionDetails(
        val description: String = "",
        val adoptionFee: Double? = null,
        val contactInfo: String = ""
    )
}
