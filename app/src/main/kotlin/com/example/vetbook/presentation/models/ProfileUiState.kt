package com.example.vetbook.presentation.models

import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User

data class ProfileUiState(
    val user: User? = null,
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = true,
    val selectedLanguage: String = "English",
    val isDarkModeEnabled: Boolean = false
)

