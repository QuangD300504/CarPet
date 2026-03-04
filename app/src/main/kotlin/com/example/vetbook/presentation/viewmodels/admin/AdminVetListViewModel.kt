package com.example.vetbook.presentation.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.domain.repository.VeterinarianRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminVetListUiState(
    val veterinarians: List<Veterinarian> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminVetListViewModel @Inject constructor(
    private val repository: VeterinarianRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminVetListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getVeterinarians()
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { list -> _uiState.update { it.copy(veterinarians = list, isLoading = false) } }
        }
    }

    fun deleteVet(id: String) {
        viewModelScope.launch {
            repository.deleteVeterinarian(id)
        }
    }
}
