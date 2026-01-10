package com.example.vetbook.presentation.models

import com.example.vetbook.domain.models.Veterinarian

data class VeterinariansUiState(
    val veterinarians: List<Veterinarian> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

