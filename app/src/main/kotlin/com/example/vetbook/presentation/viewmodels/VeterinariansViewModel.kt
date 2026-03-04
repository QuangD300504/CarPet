package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetVeterinariansUseCase
import com.example.vetbook.presentation.models.VeterinariansUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class VeterinariansViewModel @Inject constructor(
    private val getVeterinariansUseCase: GetVeterinariansUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(VeterinariansUiState())
    val uiState: StateFlow<VeterinariansUiState> = _uiState.asStateFlow()
    
    private var loadingJob: Job? = null

    init {
        loadVeterinarians()
    }
    
    override fun onCleared() {
        super.onCleared()
        loadingJob?.cancel()
    }

    private fun loadVeterinarians() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                getVeterinariansUseCase().collect { vets ->
                    _uiState.update {
                        it.copy(
                            veterinarians = vets,
                            isLoading = false,
                            error = null
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
