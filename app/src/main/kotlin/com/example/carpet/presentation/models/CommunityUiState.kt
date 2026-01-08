package com.example.carpet.presentation.models

enum class CommunityTab {
    FEED,
    ADOPTION,
    EVENTS
}

data class CommunityUiState(
    val selectedTab: CommunityTab = CommunityTab.FEED
)