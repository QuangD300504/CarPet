package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.User
import com.example.carpet.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProfileUiState(
    val user: User? = null,
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = true,
    val selectedLanguage: String = "English",
    val isDarkModeEnabled: Boolean = false
)

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = userRepository.getCurrentUser()
        val pets = if (user != null) {
            userRepository.getUserPets(user.id)
        } else {
            emptyList()
        }

        _uiState.value = _uiState.value.copy(
            user = user,
            pets = pets,
            isLoading = false
        )
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

class ProfileViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
