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

    // Track which post IDs the current user has liked (local optimistic state)
    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

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

    /**
     * Optimistically toggle like on a post, then write to Firestore.
     */
    fun toggleLike(postId: String) {
        val alreadyLiked = _likedPostIds.value.contains(postId)

        // Optimistic local update
        _likedPostIds.update { current ->
            if (alreadyLiked) current - postId else current + postId
        }
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(likesCount = post.likesCount + if (alreadyLiked) -1 else 1)
                    } else post
                }
            )
        }

        // Persist to Firestore in background
        viewModelScope.launch {
            getCommunityDataUseCase.toggleLike(postId, alreadyLiked)
                .onFailure {
                    // Revert optimistic update on failure
                    _likedPostIds.update { current ->
                        if (alreadyLiked) current + postId else current - postId
                    }
                    _uiState.update { state ->
                        state.copy(
                            posts = state.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(likesCount = post.likesCount + if (alreadyLiked) 1 else -1)
                                } else post
                            }
                        )
                    }
                }
        }
    }
}
