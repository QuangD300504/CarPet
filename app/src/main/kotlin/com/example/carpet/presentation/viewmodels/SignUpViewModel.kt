package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
 import com.example.carpet.domain.usecases.ValidateSignUpUseCase
import com.example.carpet.presentation.models.SignUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignUpViewModel(
    private val validateSignUpUseCase: ValidateSignUpUseCase = ValidateSignUpUseCase()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRePasswordChange(rePassword: String) {
        _uiState.update { it.copy(rePassword = rePassword, errorMessage = null) }
    }

    fun onSignInClick() {
        val currentState = _uiState.value
        when (val result = validateSignUpUseCase(
            username = currentState.username,
            password = currentState.password,
            rePassword = currentState.rePassword
        )) {
            is ValidateSignUpUseCase.ValidationResult.Success -> {
                _uiState.update { it.copy(errorMessage = null, isSuccess = true) }
            }
            is ValidateSignUpUseCase.ValidationResult.Error -> {
                _uiState.update { it.copy(errorMessage = result.message, isSuccess = false) }
            }
        }
    }
}
