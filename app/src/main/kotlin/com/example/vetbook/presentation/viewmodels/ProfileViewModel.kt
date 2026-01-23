package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetUserProfileUseCase
import com.example.vetbook.presentation.models.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
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
            } catch (e: Exception) {
                // Handle any errors gracefully
                _uiState.value = _uiState.value.copy(
                    user = null,
                    pets = emptyList(),
                    isLoading = false
                )
            }
        }
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(
            isDarkModeEnabled = !_uiState.value.isDarkModeEnabled
        )
    }

    fun logout() {
        _uiState.value = ProfileUiState()
    }
}
