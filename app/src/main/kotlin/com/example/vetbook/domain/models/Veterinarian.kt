package com.example.vetbook.domain.models

data class Veterinarian(
    val id: String,
    val name: String,
    val specialty: String,
    val experience: String,
    val rating: String = "New",
    val reviewsCount: Int = 0,
    val initials: String,
    val bio: String = "",
    val imageUrl: String? = null,
    val clinicId: String = ""
)

