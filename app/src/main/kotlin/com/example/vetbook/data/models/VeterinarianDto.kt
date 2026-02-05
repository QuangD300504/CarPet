package com.example.vetbook.data.models

/**
 * Firestore document in `veterinarians` collection.
 */
data class VeterinarianDto(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val experience: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val initials: String = "",
    val bio: String = "",
    val imageUrl: String? = null,
    val email: String = "",
    val phone: String = "",
    val clinicId: String? = null,
    val clinic: Clinic? = null,
    val availability: Availability? = null,
    val isActive: Boolean = true,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    data class Clinic(
        val name: String = "",
        val address: Address = Address(),
        val coordinates: Coordinates? = null
    )

    data class Address(
        val street: String = "",
        val city: String = "",
        val state: String = "",
        val zipCode: String = ""
    )

    data class Coordinates(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )

    data class Availability(
        val days: List<String> = emptyList(),
        val hours: Hours = Hours()
    )

    data class Hours(
        val start: String = "",
        val end: String = ""
    )
}
