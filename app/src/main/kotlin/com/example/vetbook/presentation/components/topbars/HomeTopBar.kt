package com.example.vetbook.presentation.components.topbars

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vetbook.presentation.components.store.StoreHeader

/**
 * Home top bar reusing the StoreHeader design so Home and Store match.
 */
@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    currentLocation: String = "Ho Chi Minh City",
    onLocationClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    profileImageUrl: String? = null,
    searchPlaceholder: String = "Search for a service",
    searchValue: String = "",
    onSearchChange: (String) -> Unit = {}
) {
    StoreHeader(
        currentLocation = currentLocation,
        onLocationClick = onLocationClick,
        onCartClick = onCartClick,
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        profileImageUrl = profileImageUrl,
        showSearchBar = true,
        searchPlaceholder = searchPlaceholder,
        onSearchChange = onSearchChange,
        searchValue = searchValue,
        modifier = modifier
    )
}
