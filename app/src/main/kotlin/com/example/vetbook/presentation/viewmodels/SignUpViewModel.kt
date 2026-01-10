package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.usecases.ValidateSignUpUseCase
import com.example.vetbook.presentation.models.SignUpFormState
import com.example.vetbook.presentation.models.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val validateSignUpUseCase: ValidateSignUpUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(SignUpFormState())
    val formState: StateFlow<SignUpFormState> = _formState.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    fun onFullNameChange(value: String) {
        _formState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _formState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _formState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _formState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onTermsChecked(value: Boolean) {
        _formState.update { it.copy(isTermsAccepted = value, errorMessage = null) }
    }

    fun onSignUpClick() {
        val form = _formState.value
        val validationResult = validateSignUpUseCase(
            fullName = form.fullName,
            email = form.email,
            phoneNumber = form.phoneNumber,
            password = form.password,
            isTermsAccepted = form.isTermsAccepted
        )

        when (validationResult) {
            is ValidateSignUpUseCase.ValidationResult.Success -> {
                performSignUp(form)
            }
            is ValidateSignUpUseCase.ValidationResult.Error -> {
                val errorMsg = when (validationResult.field) {
                    ValidateSignUpUseCase.Field.FULL_NAME -> "Vui lòng nhập tên hợp lệ"
                    ValidateSignUpUseCase.Field.EMAIL -> "Định dạng email không hợp lệ"
                    ValidateSignUpUseCase.Field.PHONE -> "Số điện thoại không hợp lệ"
                    ValidateSignUpUseCase.Field.PASSWORD -> "Mật khẩu phải có ít nhất 8 ký tự bao gồm chữ và số"
                    ValidateSignUpUseCase.Field.TERMS -> "Bạn phải đồng ý với Điều khoản và Điều kiện"
                }
                _formState.update { it.copy(errorMessage = errorMsg) }
                _uiState.value = SignUpUiState.Error(errorMsg)
            }
        }
    }

    private fun performSignUp(form: SignUpFormState) {
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            val result = authRepository.signUp(
                fullName = form.fullName,
                email = form.email,
                phone = form.phoneNumber,
                password = form.password
            )
            
            if (result.isSuccess) {
                startTimer()
                _uiState.value = SignUpUiState.Success
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Đăng ký thất bại"
                _formState.update { it.copy(errorMessage = errorMsg) }
                _uiState.value = SignUpUiState.Error(errorMsg)
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (_timerSeconds.value > 0) return
        
        viewModelScope.launch {
            _formState.update { it.copy(errorMessage = null, successMessage = null) }
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                startTimer()
                // Note: Firebase returns success even if email doesn't exist (for security)
                // Show success message - email will be sent if account exists
                _formState.update { 
                    it.copy(
                        errorMessage = null,
                        successMessage = "Email đặt lại mật khẩu đã được gửi đến $email. Vui lòng kiểm tra hộp thư của bạn."
                    ) 
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage 
                    ?: result.exceptionOrNull()?.message 
                    ?: "Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại."
                _formState.update { it.copy(errorMessage = errorMsg, successMessage = null) }
            }
        }
    }

    fun resendVerificationEmail() {
        if (_timerSeconds.value > 0) return
        
        viewModelScope.launch {
            _formState.update { it.copy(errorMessage = null, successMessage = null) }
            
            // Check if user is signed in before attempting to send verification
            if (!authRepository.isUserLoggedIn()) {
                _formState.update { 
                    it.copy(errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để gửi email xác minh.") 
                }
                return@launch
            }
            
            val result = authRepository.sendEmailVerification()
            if (result.isSuccess) {
                startTimer()
                val userEmail = authRepository.getCurrentUser()?.email ?: formState.value.email
                _formState.update { 
                    it.copy(
                        errorMessage = null,
                        successMessage = "Email xác minh đã được gửi đến $userEmail. Vui lòng kiểm tra hộp thư của bạn."
                    ) 
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage 
                    ?: result.exceptionOrNull()?.message 
                    ?: "Không thể gửi email xác minh. Vui lòng thử lại."
                _formState.update { it.copy(errorMessage = errorMsg, successMessage = null) }
            }
        }
    }

    private fun startTimer() {
        _timerSeconds.value = 30
        viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                delay(1000)
                _timerSeconds.value -= 1
            }
        }
    }

    fun checkVerification(onVerified: () -> Unit) {
        viewModelScope.launch {
            val isVerified = authRepository.isEmailVerified()
            if (isVerified) {
                onVerified()
            } else {
                _formState.update { it.copy(errorMessage = "Email chưa được xác minh. Vui lòng kiểm tra hộp thư.") }
            }
        }
    }
}
