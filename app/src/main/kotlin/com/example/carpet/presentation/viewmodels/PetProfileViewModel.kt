package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.repository.UserRepository
import com.example.carpet.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * UI State for Pet Profile Screen
 */
data class PetProfileUiState(
    val pet: Pet? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for PetProfileScreen
 * Manages pet details and vaccination records
 */
class PetProfileViewModel(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    private val petId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState(isLoading = true))
    val uiState: StateFlow<PetProfileUiState> = _uiState

    init {
        loadPetDetails()
    }

    private fun loadPetDetails() {
        try {
            // 1. First, search in User's pets
            val currentUser = userRepository.getCurrentUser()
            var selectedPet: Pet? = null
            
            if (currentUser != null) {
                selectedPet = userRepository.getUserPets(currentUser.id).find { it.id == petId }
            }

            // 2. If not found, search in Community adoption pets
            if (selectedPet == null) {
                // Using runBlocking here for simplicity since mock repo uses flowOf
                val adoptionPets = runBlocking { communityRepository.getAdoptionPets().first() }
                selectedPet = adoptionPets.find { it.id == petId }
            }
            
            _uiState.value = if (selectedPet != null) {
                PetProfileUiState(pet = selectedPet, isLoading = false)
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

/**
 * Factory for creating PetProfileViewModel instances
 */
class PetProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    private val petId: String
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PetProfileViewModel(userRepository, communityRepository, petId) as T
    }
}
