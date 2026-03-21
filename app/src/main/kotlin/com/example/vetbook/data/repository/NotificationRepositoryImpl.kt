package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.NotificationDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.data.network.InstantPushBody
import com.example.vetbook.data.network.SubscribePushBody
import com.example.vetbook.data.network.WorkerApiService
import com.example.vetbook.domain.models.Notification
import com.example.vetbook.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationDataSource,
    private val workerApiService: WorkerApiService,
    private val auth: FirebaseAuth
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

    override suspend fun subscribeToPush(fcmToken: String) {
        val idToken: String? = try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (_: Exception) { null }
        if (idToken.isNullOrBlank()) return
        try {
            workerApiService.subscribeToPush("Bearer $idToken", SubscribePushBody(fcmToken))
        } catch (e: Exception) {
            // Log but don't throw — push subscription is best-effort
        }
    }

    override suspend fun unsubscribeFromPush() {
        val idToken: String? = try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (_: Exception) { null }
        if (idToken.isNullOrBlank()) return
        try {
            workerApiService.unsubscribeFromPush("Bearer $idToken")
        } catch (e: Exception) {
            // Best-effort
        }
    }

    override suspend fun triggerInstantPush(type: String, refId: String) {
        val idToken: String? = try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (_: Exception) { null }
        if (idToken.isNullOrBlank()) return
        try {
            workerApiService.triggerInstantPush("Bearer $idToken", InstantPushBody(type, refId))
        } catch (e: Exception) {
            // Log but don't fail the parent operation
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid
}
