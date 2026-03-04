package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.NotificationDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.Notification
import com.example.vetbook.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationDataSource
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<List<Notification>> {
        return remoteDataSource.getNotifications(userId).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return remoteDataSource.getUnreadCount(userId)
    }

    override suspend fun markAsRead(notificationId: String) {
        remoteDataSource.markAsRead(notificationId)
    }

    override suspend fun dismissNotification(notificationId: String) {
        remoteDataSource.dismissNotification(notificationId)
    }
}
