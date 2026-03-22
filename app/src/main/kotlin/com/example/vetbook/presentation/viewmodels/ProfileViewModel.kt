package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.domain.usecases.GetUserProfileUseCase
import com.example.vetbook.domain.usecases.UpdateUserAvatarUseCase
import com.example.vetbook.presentation.models.ProfileUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val bookingRepository: BookingRepository 
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
                    var count = 0
                    bookingRepository.getUserAppointments(uid).collect { appointments ->
                        count = appointments.count {
                            it.status == "UPCOMING" || it.status == "PENDING_PAYMENT"
                        }
                    }
                    count
                } catch (_: Exception) { 0 }

                _uiState.update {
                    it.copy(
                        user = result?.first,
                        pets = result?.second ?: emptyList(),
                        upcomingAppointmentCount = upcomingCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(user = null, pets = emptyList(), isLoading = false) }
            }
        }
    }

    /**
     * Persists fullName + phone to Firestore and updates local state immediately.
     */
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

    /**
     * Sends a Firebase password-reset email to the signed-in user's address.
     */
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

    /**
     * Permanently deletes the Firebase Auth account.
     * Firebase requires recent authentication — if this fails with
     * "requires-recent-login", prompt the user to sign out and sign back in first.
     */
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