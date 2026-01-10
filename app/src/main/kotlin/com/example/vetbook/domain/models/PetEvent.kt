package com.example.vetbook.domain.models

data class PetEvent(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val imageUrl: String?,
    val iconRes: Int? = null
)
