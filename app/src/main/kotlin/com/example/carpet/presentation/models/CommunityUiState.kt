package com.example.carpet.presentation.models

import com.example.carpet.domain.models.Pet
import com.example.carpet.domain.models.PetEvent
import com.example.carpet.domain.models.Post

enum class CommunityTab {
    Feed, Adoption, Events
}

data class CommunityUiState(
    val posts: List<Post> = emptyList(),
    val pets: List<Pet> = emptyList(),
    val events: List<PetEvent> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: CommunityTab = CommunityTab.Feed
)