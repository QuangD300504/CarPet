package com.example.vetbook.data.models

import androidx.annotation.Keep

@Keep
data class ServicePackageDto(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val durationMinutes: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long? = null
)
@Keep
data class PetServiceDetailDto(
    val categoryId: String = "",
    val rating: Double = 0.0,
    val reviewCount: String = "",
    val about: String = "",
    val packages: List<ServicePackageDto> = emptyList(),
    val availableTimes: List<String> = emptyList(),
    val bannerGradientColors: List<Long> = emptyList()
)


