package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.NotificationDto
import kotlinx.coroutines.flow.Flow

interface NotificationDataSource {
    fun getNotifications(userId: String): Flow<List<NotificationDto>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(notificationId: String)
    suspend fun dismissNotification(notificationId: String)
}
