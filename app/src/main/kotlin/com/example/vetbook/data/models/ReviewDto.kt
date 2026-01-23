package com.example.vetbook.data.models

/**
 * Document in `reviews` collection, linked to veterinarians and users.
 */
data class ReviewDto(
    val id: String = "",
    val userId: String = "",
    val veterinarianId: String = "",
    val appointmentId: String? = null,
    val rating: Int = 0,
    val title: String? = null,
    val comment: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isVerified: Boolean = false,
    val helpfulCount: Int = 0
)


