package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.NotificationDataSource
import com.example.vetbook.data.models.NotificationDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseNotificationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationDataSource {

    override fun getNotifications(userId: String): Flow<List<NotificationDto>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { it.toObject(NotificationDto::class.java)?.copy(id = it.id) }
                        .sortedByDescending { it.createdAt }
                    trySend(notifications)
                }
            }
            
        awaitClose { listener.remove() }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true).await()
        } catch (_: Exception) {}
    }

    override suspend fun dismissNotification(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .delete().await()
        } catch (_: Exception) {}
    }
}
