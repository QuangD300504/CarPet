package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContinueLoginUiState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val email: String = "",
    val error: String? = null
)

@HiltViewModel
class ContinueLoginViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContinueLoginUiState())
    val uiState: StateFlow<ContinueLoginUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pair = getUserProfileUseCase()
                if (pair == null) {
                    _uiState.update { it.copy(isLoading = false, error = "User not found") }
                } else {
                    val user = pair.first
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fullName = user.name,
                            email = user.email,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load user") }
            }
        }
    }
}
