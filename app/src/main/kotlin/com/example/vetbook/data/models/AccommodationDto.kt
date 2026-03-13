package com.example.vetbook.data.models

data class AccommodationDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val location: String = "",
    val district: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val price: Double = 0.0,
    val priceUnit: String = "USD",
    val imageUrl: String? = null,
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isPopular: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
