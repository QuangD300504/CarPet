package com.example.vetbook.domain.models

import androidx.annotation.Keep
import java.time.Instant
@Keep
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
@Keep
enum class NotificationType {
    INFO,      // Blue icon
    INCIDENT,  // Red icon with exclamation
    REPLY      // Blue icon
}
