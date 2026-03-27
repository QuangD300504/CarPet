package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.domain.repository.NotificationRepository
import com.example.vetbook.domain.usecases.GetUserProfileUseCase
import com.example.vetbook.notification.ReminderNotificationHelper
import com.example.vetbook.domain.usecases.UpdateUserAvatarUseCase
import com.example.vetbook.presentation.models.ProfileUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val application: Application,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val bookingRepository: BookingRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init { loadProfileData() }

    fun refresh() = loadProfileData()

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = getUserProfileUseCase()
                val uid = auth.currentUser?.uid ?: ""
                val upcomingCount = try {
                    // FIX: getUserAppointments() is a callbackFlow (addSnapshotListener) —
                    // an infinite, never-completing Flow. The previous .collect {} suspended
                    // forever so _uiState.update was never reached.
                    // .first() takes the initial emission then cancels the subscription.
                    bookingRepository.getUserAppointments(uid)
                        .first()
                        .count { it.status == "UPCOMING" || it.status == "PENDING_PAYMENT" }
                } catch (_: Exception) { 0 }

                _uiState.update {
                    it.copy(
                        user = result?.first?.let { u ->
                            if (u.email.isBlank()) u.copy(email = auth.currentUser?.email ?: "") else u
                        },
                        pets = result?.second ?: emptyList(),
                        upcomingAppointmentCount = upcomingCount,
                        notificationsEnabled = true, // default; overridden by Firestore read below
                        isLoading = false
                    )
                }
                // Read notifications preference from Firestore
                if (uid.isNotBlank()) {
                    val profile = remoteUserDataSource.getUserProfile(uid)
                    val notifEnabled = profile?.preferences?.notificationsEnabled ?: true
                    _uiState.update { it.copy(notificationsEnabled = notifEnabled) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(user = null, pets = emptyList(), isLoading = false) }
            }
        }
    }

    fun saveProfile(fullName: String, phone: String, onResult: (success: Boolean, message: String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "Chưa đăng nhập")
        val trimmedName = fullName.trim()
        if (trimmedName.isBlank()) return onResult(false, "Tên không được để trống")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                remoteUserDataSource.updateUserProfileFields(
                    uid = uid,
                    fields = mapOf(
                        "fullName" to trimmedName,
                        "phone"   to phone.trim(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        user = state.user?.copy(name = trimmedName)
                    )
                }
                onResult(true, "Đã lưu thông tin")
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onResult(false, "Lỗi: ${e.message}")
            }
        }
    }

    fun sendPasswordResetEmail(onResult: (success: Boolean, message: String) -> Unit) {
        val email = auth.currentUser?.email
        if (email.isNullOrBlank()) return onResult(false, "Không tìm thấy email")
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                onResult(true, "Email đặt lại mật khẩu đã gửi tới $email")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Gửi email thất bại")
            }
        }
    }

    fun deleteAccount(onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                onResult(true, "Tài khoản đã được xóa")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Xóa tài khoản thất bại")
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch {
            try {
                remoteUserDataSource.updateUserProfileFields(
                    uid = uid,
                    fields = mapOf("preferences.notificationsEnabled" to enabled)
                )
                // When turning ON, immediately schedule/show reminders for each upcoming appointment
                if (enabled) {
                    try {
                        val upcoming = bookingRepository.getUserAppointments(uid)
                            .first()
                            .filter { it.status == "UPCOMING" || it.status == "PENDING_PAYMENT" }
                        val now = System.currentTimeMillis()
                        upcoming.forEach { appt ->
                            val apptMillis = appt.appointmentAt.toEpochMilli()
                            val reminderMillis = apptMillis - (24 * 60 * 60 * 1000L)
                            val apptFormatted = java.text.SimpleDateFormat(
                                "dd/MM/yyyy HH:mm", java.util.Locale.getDefault()
                            ).format(java.util.Date(apptMillis))
                            val petName = appt.petNames.firstOrNull() ?: "Thú cưng"
                            if (reminderMillis > now) {
                                // More than 24h away — schedule WorkManager reminder
                                ReminderNotificationHelper.scheduleAppointmentReminder(
                                    context = application,
                                    workName = "appointment_reminder_${appt.id}",
                                    vetName = appt.veterinarianName,
                                    petName = petName,
                                    appointmentTime = apptFormatted,
                                    reminderTimeMillis = reminderMillis
                                )
                            } else if (apptMillis > now) {
                                // Less than 24h away — show notification immediately
                                ReminderNotificationHelper.showNotification(
                                    context = application,
                                    title = "Nhắc lịch hẹn hôm nay",
                                    body = "Lịch khám với ${appt.veterinarianName} lúc $apptFormatted cho $petName"
                                )
                            }
                        }
                    } catch (_: Exception) { }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(notificationsEnabled = !enabled) }
            }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkModeEnabled = !it.isDarkModeEnabled) }
    }

    fun logout() { _uiState.value = ProfileUiState() }

    fun uploadAvatar(imageBytes: ByteArray, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = updateUserAvatarUseCase(imageBytes)
            result.onSuccess { url ->
                _uiState.update { it.copy(user = it.user?.copy(profileImageUrl = url), isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
            onResult(result)
        }
    }
}