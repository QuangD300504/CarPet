package com.example.vetbook.presentation.models

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

data class SignUpFormState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isTermsAccepted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
