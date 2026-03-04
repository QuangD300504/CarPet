package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Notification
import com.example.vetbook.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

data class NotificationUiItem(
    val notification: Notification,
    val timeAgo: String
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationUiItem>>(emptyList())
    val notifications: StateFlow<List<NotificationUiItem>> = _notifications.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.getNotifications(userId).collect { list ->
                // Map the domain models to UI items with formatted `timeAgo` strings
                val uiList = list.map { notification ->
                    NotificationUiItem(
                        notification = notification,
                        timeAgo = formatTimeAgo(notification.createdAt)
                    )
                }
                _notifications.value = uiList
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun dismissNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.dismissNotification(notificationId)
        }
    }

    private fun formatTimeAgo(createdAt: Instant): String {
        val now = Instant.now()
        val duration = Duration.between(createdAt, now)
        
        return when {
            duration.toMinutes() < 1 -> "NOW"
            duration.toHours() < 1 -> "${duration.toMinutes()} MIN AGO"
            duration.toDays() < 1 -> "${duration.toHours()} HOURS AGO"
            duration.toDays() < 7 -> "${duration.toDays()} DAYS AGO"
            else -> "A WHILE AGO"
        }
    }
}
