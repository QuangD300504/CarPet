package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onRePasswordChange(rePassword: String) {
        _uiState.update { it.copy(rePassword = rePassword) }
    }

    fun onSignInClick() {
        val currentState = _uiState.value
        when {
            currentState.username.isBlank() || currentState.password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Fields cannot be empty") }
            }
            currentState.password != currentState.rePassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            }
            else -> {
                _uiState.update { it.copy(errorMessage = null, isSuccess = true) }
            }
        }
    }
}
