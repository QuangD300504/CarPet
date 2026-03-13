package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteVaccinationDataSource
import com.example.vetbook.domain.usecases.GetPetProfileUseCase
import com.example.vetbook.presentation.models.PetProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.vetbook.data.mappers.toDomain
import javax.inject.Inject

/**
 * ViewModel for PetProfileScreen
 * Manages pet details and vaccination records
 */
@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val getPetProfileUseCase: GetPetProfileUseCase,
    private val remotePetDataSource: RemotePetDataSource,
    private val remoteVaccinationDataSource: RemoteVaccinationDataSource,
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Get basic profile
                val pet = getPetProfileUseCase(petId)
                if (pet != null) {
                // 2. Fetch vaccinations separately to ensure they are up to date
                val vaccinations = remoteVaccinationDataSource.getVaccinationsByPet(petId).map { it.toDomain() }
                _uiState.update { 
                    it.copy(
                        pet = pet.copy(vaccinations = vaccinations), 
                        isLoading = false 
                    ) 
                }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy thú cưng") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Lỗi không xác định") }
            }
        }
    }

    fun deletePet(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            remotePetDataSource.deletePet(petId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
