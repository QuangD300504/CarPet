package com.example.vetbook.domain.models

import java.time.Instant

data class Notification(
    val id: String,
    val userId: String,
    val appName: String,
    val title: String,
    val description: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAt: Instant
)

enum class NotificationType {
    INFO,      // Blue icon
    INCIDENT,  // Red icon with exclamation
    REPLY      // Blue icon
}
