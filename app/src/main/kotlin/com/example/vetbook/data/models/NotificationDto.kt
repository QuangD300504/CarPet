package com.example.vetbook.data.models

/**
 * Firestore document in `notifications` collection.
 */
data class NotificationDto(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)


