package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpet.domain.repository.VeterinarianRepository
import com.example.carpet.domain.usecases.GetVeterinariansUseCase
import com.example.carpet.presentation.models.VeterinariansUiState
import com.example.carpet.utils.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VeterinariansViewModel(
    private val getVeterinariansUseCase: GetVeterinariansUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(VeterinariansUiState())
    val uiState: StateFlow<VeterinariansUiState> = _uiState.asStateFlow()

    init {
        loadVeterinarians()
    }

    private fun loadVeterinarians() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                getVeterinariansUseCase().collect { vets ->
                    _uiState.value = VeterinariansUiState(
                        veterinarians = vets,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load veterinarians"
                )
            }
        }
    }
}

class VeterinariansViewModelFactory(
    private val repository: VeterinarianRepository
) : ViewModelFactory<VeterinariansViewModel>(
    create = { VeterinariansViewModel(GetVeterinariansUseCase(repository)) },
    viewModelClass = VeterinariansViewModel::class.java
)
