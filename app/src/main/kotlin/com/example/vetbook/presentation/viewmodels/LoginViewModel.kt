package com.example.vetbook.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.usecases.ValidateLoginUseCase
import com.example.vetbook.presentation.models.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateLoginUseCase: ValidateLoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onLoginClick() {
        val validationResult = validateLoginUseCase(
            username = _uiState.value.username,
            password = _uiState.value.password
        )

        when (validationResult) {
            ValidateLoginUseCase.ValidationResult.Success -> {
                performLogin()
            }
            is ValidateLoginUseCase.ValidationResult.EmptyFields -> {
                _uiState.update { it.copy(error = "Vui lòng điền đầy đủ thông tin") }
            }
            is ValidateLoginUseCase.ValidationResult.InvalidEmail -> {
                _uiState.update { it.copy(error = "Định dạng email không hợp lệ") }
            }
        }
    }

    fun onGoogleSignInClick(context: Context) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Google Sign In failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    private fun performLogin() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.login(_uiState.value.username, _uiState.value.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.localizedMessage ?: "Email hoặc mật khẩu không chính xác"
                    ) 
                }
            }
        }
    }
}
