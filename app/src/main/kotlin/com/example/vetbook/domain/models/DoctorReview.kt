package com.example.vetbook.domain.models

data class DoctorReview(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Long
)
