package com.example.vetbook.domain.models

import androidx.annotation.Keep

@Keep
data class Veterinarian(
    val id: String,
    val name: String,
    val specialty: String,
    val experience: String,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val initials: String,
    val bio: String = "",
    val imageUrl: String? = null,
    val clinicId: String = "",
    val servicePrice: Double = 100000.0
) {
    val ratingLabel: String
        get() = if (reviewsCount == 0) "Mới" else String.format("%.1f", rating)
}
