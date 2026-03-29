package com.example.vetbook.data.models

import androidx.annotation.Keep

/**
 * Firestore document in `services` collection.
 */
@Keep
data class ServiceCategoryDto(
    val id: String = "",
    val title: String = "",
    val shortDescription: String = "",
    val iconUrl: String? = null,
    val bannerGradientColors: List<Long> = emptyList(),
    val about: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
