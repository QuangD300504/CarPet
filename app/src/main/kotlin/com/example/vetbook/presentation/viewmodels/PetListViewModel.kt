package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetListUiState(
    val isLoading: Boolean = true,
    val pets: List<PetDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PetListViewModel @Inject constructor(
    private val remotePetDataSource: RemotePetDataSource,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetListUiState())
    val uiState: StateFlow<PetListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(isLoading = false, pets = emptyList(), errorMessage = "Not authenticated") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val pets = remotePetDataSource.getUserPets(uid)
                _uiState.update { it.copy(isLoading = false, pets = pets, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, pets = emptyList(), errorMessage = e.message ?: "Failed to load pets") }
            }
        }
    }
}
