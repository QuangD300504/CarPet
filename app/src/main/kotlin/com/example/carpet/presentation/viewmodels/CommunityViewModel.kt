package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpet.domain.repository.CommunityRepository
import com.example.carpet.domain.usecases.GetCommunityDataUseCase
import com.example.carpet.presentation.models.CommunityTab
import com.example.carpet.presentation.models.CommunityUiState
import com.example.carpet.utils.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityViewModel(
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

class CommunityViewModelFactory(
    private val repository: CommunityRepository
) : ViewModelFactory<CommunityViewModel>(
    create = { CommunityViewModel(GetCommunityDataUseCase(repository)) },
    viewModelClass = CommunityViewModel::class.java
)
