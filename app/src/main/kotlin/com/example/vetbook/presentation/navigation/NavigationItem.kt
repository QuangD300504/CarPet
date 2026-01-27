package com.example.vetbook.presentation.navigation

import com.example.vetbook.R

sealed class NavigationItem(val route: String, val label: String, val iconRes: Int) {
    object Home : NavigationItem(
        route = Routes.Home.route,
        label = "Home",
        iconRes = R.drawable.home
    )
    object Service : NavigationItem(
        route = Routes.Service.route,
        label = "Calendar",
        iconRes = R.drawable.calender
    )
    object Profile : NavigationItem(
        route = Routes.Profile.route,
        label = "Profile",
        iconRes = R.drawable.profile
    )
    object Store : NavigationItem(
        route = Routes.Store.route,
        label = "Store",
        iconRes = R.drawable.store
    )
    object Pet : NavigationItem(
        route = Routes.Pet.route,
        label = "Pet",
        iconRes = R.drawable.pet
    )
}

val bottomNavItems = listOf(
    NavigationItem.Home,
    NavigationItem.Service,
    NavigationItem.Store,
    NavigationItem.Pet
)