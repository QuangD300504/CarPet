package com.example.vetbook.presentation.models

import com.example.vetbook.domain.models.Pet

/**
 * UI State for Pet Profile Screen
 */
data class PetProfileUiState(
    val pet: Pet? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

