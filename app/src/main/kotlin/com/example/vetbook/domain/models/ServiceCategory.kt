package com.example.vetbook.domain.models

import androidx.annotation.Keep

/**
 * Representing a major service category on the main Service Screen.
 * Based on the Pitchdeck's 3-in-1 solution.
 */
@Keep
data class ServiceCategory(
    val id: String,
    val title: String,
    val shortDescription: String,
    val iconRes: Int,
    val iconUrl: String? = null,
    val imageUrl: String? = null
)
@Keep
data class ServicePackage(
    val id: String,
    val name: String,
    val price: Double
)
@Keep
data class PetServiceDetail(
    val categoryId: String,
    val rating: Float,
    val reviewCount: String,
    val about: String,
    val packages: List<ServicePackage>,
    val availableTimes: List<String>,
    val bannerGradientColors: List<Long>
)