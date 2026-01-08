package com.example.carpet.presentation.models

data class SignUpUiState(
    val username: String = "",
    val password: String = "",
    val rePassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
