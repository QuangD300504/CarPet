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
    currentLocation: String = "Hồ Chí Minh",
    onLocationClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    profileImageUrl: String? = null,
    searchPlaceholder: String = "Tìm kiếm dịch vụ...",
    searchValue: String = "",
    onSearchChange: (String) -> Unit = {},
    hasUnreadNotifications: Boolean = false
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
        hasUnreadNotifications = hasUnreadNotifications,
        modifier = modifier
    )
}
