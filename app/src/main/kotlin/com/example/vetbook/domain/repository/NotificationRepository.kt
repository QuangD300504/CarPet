package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(notificationId: String)
    suspend fun dismissNotification(notificationId: String)
}
