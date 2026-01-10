package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetCommunityDataUseCase
import com.example.vetbook.presentation.models.CommunityTab
import com.example.vetbook.presentation.models.CommunityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityDataUseCase: GetCommunityDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            launch {
                getCommunityDataUseCase.getPosts().collect { posts ->
                    _uiState.update { it.copy(posts = posts) }
                }
            }
            
            launch {
                getCommunityDataUseCase.getAdoptionPets().collect { pets ->
                    _uiState.update { it.copy(pets = pets) }
                }
            }
            
            launch {
                getCommunityDataUseCase.getEvents().collect { events ->
                    _uiState.update { it.copy(events = events) }
                }
            }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onTabSelected(tab: CommunityTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
