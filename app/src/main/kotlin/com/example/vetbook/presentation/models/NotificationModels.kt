package com.example.vetbook.presentation.models

data class Notification(
    val id: String,
    val appName: String,
    val timeAgo: String,
    val title: String,
    val description: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    INFO,      // Blue icon
    INCIDENT,  // Red icon with exclamation
    REPLY      // Blue icon
}

