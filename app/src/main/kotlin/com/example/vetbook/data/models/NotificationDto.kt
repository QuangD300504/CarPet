package com.example.vetbook.data.models

import com.google.firebase.Timestamp

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
