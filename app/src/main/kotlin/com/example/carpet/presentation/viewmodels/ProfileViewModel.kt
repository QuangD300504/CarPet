package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.carpet.domain.repository.UserRepository
import com.example.carpet.domain.usecases.GetUserProfileUseCase
import com.example.carpet.presentation.models.ProfileUiState
import com.example.carpet.utils.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val result = getUserProfileUseCase()
        if (result != null) {
            val (user, pets) = result
            _uiState.value = _uiState.value.copy(
                user = user,
                pets = pets,
                isLoading = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                user = null,
                pets = emptyList(),
                isLoading = false
            )
        }
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(
            isDarkModeEnabled = !_uiState.value.isDarkModeEnabled
        )
    }

    fun logout() {
        // Reset profile state when logging out
        _uiState.value = ProfileUiState()
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelFactory<ProfileViewModel>(
    create = { ProfileViewModel(GetUserProfileUseCase(userRepository)) },
    viewModelClass = ProfileViewModel::class.java
)
