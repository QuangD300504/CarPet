package com.example.carpet.presentation.models

import com.example.carpet.domain.models.Veterinarian

data class VeterinariansUiState(
    val veterinarians: List<Veterinarian> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

