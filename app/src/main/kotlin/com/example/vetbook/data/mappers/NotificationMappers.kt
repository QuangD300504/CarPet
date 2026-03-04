package com.example.vetbook.data.mappers

import com.example.vetbook.data.models.NotificationDto
import com.example.vetbook.domain.models.Notification
import com.example.vetbook.domain.models.NotificationType
import java.time.Instant

fun NotificationDto.toDomain(): Notification {
    val enumType = try {
        NotificationType.valueOf(this.type)
    } catch (e: Exception) {
        NotificationType.INFO
    }

    return Notification(
        id = this.id,
        userId = this.userId,
        appName = this.appName,
        title = this.title,
        description = this.description,
        type = enumType,
        isRead = this.isRead,
        createdAt = Instant.ofEpochSecond(this.createdAt.seconds, this.createdAt.nanoseconds.toLong())
    )
}
