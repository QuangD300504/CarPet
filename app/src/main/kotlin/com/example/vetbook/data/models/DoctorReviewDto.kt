package com.example.vetbook.data.models

import androidx.annotation.Keep

@Keep
data class DoctorReviewDto(
    val id: String = "",
    val appointmentId: String = "",
    val doctorId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
