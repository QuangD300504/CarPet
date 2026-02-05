package com.example.vetbook.data.models

/**
 * Firestore document in `clinics` collection.
 */
data class ClinicDto(
    val id: String = "",
    val name: String = "",
    val address: Address = Address(),
    val coordinates: Coordinates? = null,
    val imageUrl: String? = null,
    val phone: String = "",
    val email: String = "",
    val website: String? = null,
    val operatingHours: Map<String, String> = emptyMap(), // e.g., "monday" to "9am-5pm"
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
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
}
