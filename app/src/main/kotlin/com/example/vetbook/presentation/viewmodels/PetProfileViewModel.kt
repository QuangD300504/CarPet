package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetPetProfileUseCase
import com.example.vetbook.presentation.models.PetProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for PetProfileScreen
 * Manages pet details and vaccination records
 */
@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val getPetProfileUseCase: GetPetProfileUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow(PetProfileUiState(isLoading = true))
    val uiState: StateFlow<PetProfileUiState> = _uiState

    init {
        loadPetDetails()
    }

    private fun loadPetDetails() {
        viewModelScope.launch {
            try {
                val pet = getPetProfileUseCase(petId)
                _uiState.value = if (pet != null) {
                    PetProfileUiState(pet = pet, isLoading = false)
                } else {
                    PetProfileUiState(
                        isLoading = false,
                        error = "Pet not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PetProfileUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}
