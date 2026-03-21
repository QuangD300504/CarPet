package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(notificationId: String)
    suspend fun dismissNotification(notificationId: String)
    /** Sends the FCM registration token to the backend Worker for push notifications. */
    suspend fun subscribeToPush(fcmToken: String)
    /** Removes the FCM token from the backend (e.g. on logout). */
    suspend fun unsubscribeFromPush()
    /**
     * Triggers an instant push notification from the server.
     * Used after booking/saving to notify the user immediately.
     *
     * @param type  "vaccine" or "appointment"
     * @param refId ID of the vaccination or appointment record
     */
    suspend fun triggerInstantPush(type: String, refId: String)
    /** Returns the logged-in user's UID synchronously, or null if not authenticated yet. */
    fun getCurrentUserId(): String?
}
