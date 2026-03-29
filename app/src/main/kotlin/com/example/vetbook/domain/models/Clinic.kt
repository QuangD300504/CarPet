package com.example.vetbook.domain.models

import androidx.annotation.Keep

@Keep
data class Clinic(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = ""
)
