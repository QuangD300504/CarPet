package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetVeterinariansUseCase
import com.example.vetbook.presentation.models.VeterinariansUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VeterinariansViewModel @Inject constructor(
    private val getVeterinariansUseCase: GetVeterinariansUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(VeterinariansUiState())
    val uiState: StateFlow<VeterinariansUiState> = _uiState.asStateFlow()

    init {
        loadVeterinarians()
    }

    private fun loadVeterinarians() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getVeterinariansUseCase().collect { vets ->
                    _uiState.update {
                        VeterinariansUiState(
                            veterinarians = vets,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load veterinarians"
                    )
                }
            }
        }
    }
}
