package com.example.carpet.presentation.models

import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.User

data class ProfileUiState(
    val user: User? = null,
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = true,
    val selectedLanguage: String = "English",
    val isDarkModeEnabled: Boolean = false
)

