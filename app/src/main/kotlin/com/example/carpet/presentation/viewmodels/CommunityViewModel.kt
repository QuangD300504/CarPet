package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.PetEvent
import com.example.carpet.domain.models.Post
import com.example.carpet.domain.repository.CommunityRepository
import com.example.carpet.data.repository.MockCommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<Post> = emptyList(),
    val adoptionPets: List<Pet> = emptyList(),
    val events: List<PetEvent> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: CommunityTab = CommunityTab.Feed
)

enum class CommunityTab {
    Feed, Adoption, Events
}

class CommunityViewModel(
    private val repository: CommunityRepository = MockCommunityRepository()
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
                repository.getPosts().collect { posts ->
                    _uiState.update { it.copy(posts = posts) }
                }
            }
            
            launch {
                repository.getAdoptionPets().collect { pets ->
                    _uiState.update { it.copy(adoptionPets = pets) }
                }
            }
            
            launch {
                repository.getEvents().collect { events ->
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
