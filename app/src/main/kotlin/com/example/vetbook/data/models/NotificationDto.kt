package com.example.vetbook.data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
@Keep
data class NotificationDto(
    val id: String = "",
    val userId: String = "",
    val appName: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "INFO",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
