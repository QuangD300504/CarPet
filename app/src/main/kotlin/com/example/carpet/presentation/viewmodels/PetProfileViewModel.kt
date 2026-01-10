package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpet.domain.repository.CommunityRepository
import com.example.carpet.domain.repository.UserRepository
import com.example.carpet.domain.usecases.GetPetProfileUseCase
import com.example.carpet.presentation.models.PetProfileUiState
import com.example.carpet.utils.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for PetProfileScreen
 * Manages pet details and vaccination records
 */
class PetProfileViewModel(
    private val getPetProfileUseCase: GetPetProfileUseCase,
    private val petId: String
) : ViewModel() {

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

/**
 * Factory for creating PetProfileViewModel instances
 */
class PetProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    private val petId: String
) : ViewModelFactory<PetProfileViewModel>(
    create = {
        PetProfileViewModel(
            getPetProfileUseCase = GetPetProfileUseCase(userRepository, communityRepository),
            petId = petId
        )
    },
    viewModelClass = PetProfileViewModel::class.java
)
