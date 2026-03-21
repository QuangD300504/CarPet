package com.example.vetbook.presentation.viewmodels

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SharedNotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    private val _isSubscribing = MutableStateFlow(false)
    val isSubscribing: StateFlow<Boolean> = _isSubscribing.asStateFlow()

    fun startListening(userId: String) {
        viewModelScope.launch {
            notificationRepository.getUnreadCount(userId).collect { unreadCount ->
                _hasUnread.value = unreadCount > 0
            }
        }
    }

    /**
     * Checks Android 13+ POST_NOTIFICATIONS permission.
     * If already granted → proceeds to get FCM token and subscribe.
     * If not yet granted → requests permission (system dialog), result
     * comes through onRequestPermissionsResult → MainActivity delegates here.
     */
    fun subscribeToPushWithPermission(
        activity: ComponentActivity,
        onPermissionDenied: () -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                // Save the deny callback, request permission
                pendingDenyCallback = onPermissionDenied
                activity.requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                return
            }
        }
        // Already granted or pre-Android 13 — subscribe directly
        doSubscribe()
    }

    private var pendingDenyCallback: (() -> Unit)? = null

    init {
        // Subscribe to permission results posted by MainActivity
        viewModelScope.launch {
            permissionResultChannel.receiveAsFlow().collect { granted ->
                if (granted) {
                    doSubscribe()
                } else {
                    pendingDenyCallback?.invoke()
                    pendingDenyCallback = null
                }
            }
        }
    }

    private fun doSubscribe() {
        val currentUser = notificationRepository.getCurrentUserId()
        if (currentUser == null) {
            android.util.Log.w("SharedNotifVM", "User not yet loaded by Firebase Auth — skipping subscribe")
            return
        }
        viewModelScope.launch {
            _isSubscribing.value = true
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.subscribeToPush(token)
            } catch (e: Exception) {
                android.util.Log.e("SharedNotifVM", "FCM subscribe failed", e)
            } finally {
                _isSubscribing.value = false
                pendingDenyCallback = null
            }
        }
    }

    /** Removes the FCM token from the backend (e.g. on logout). */
    fun unsubscribeFromPush() {
        viewModelScope.launch {
            try {
                notificationRepository.unsubscribeFromPush()
            } catch (_: Exception) {}
        }
    }

    companion object {
        const val REQUEST_NOTIFICATION_PERMISSION = 9001

        /**
         * Single-use channel shared between MainActivity and all SharedNotificationViewModel instances.
         * MainActivity posts permission results here; any ViewModel instance collects it.
         */
        private val _permissionResultChannel = kotlinx.coroutines.channels.Channel<Boolean>(1)
        val permissionResultChannel get() = _permissionResultChannel
    }
}
