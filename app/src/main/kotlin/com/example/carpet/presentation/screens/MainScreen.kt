package com.example.carpet.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.components.CarPetBottomBar
import com.example.carpet.presentation.components.topbars.HomeTopBar
import com.example.carpet.presentation.navigation.Routes
import com.example.carpet.presentation.viewmodels.HomeViewModel

@Composable
fun MainScreen(homeViewModel: HomeViewModel = viewModel()) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (currentRoute == Routes.Home.route) {
                HomeTopBar(hasNotification = homeUiState.hasNotification)
            } else {
                Text(text = "Top Bar for $currentRoute")
            }
        },
        bottomBar = {
            CarPetBottomBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home.route) {
                HomeScreenContent(
                    uiState = homeUiState,
                    onSeeAllClick = {
                        bottomNavController.navigate(Routes.Service.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Routes.Service.route) {
                Text(text = "Service Screen")
            }
            composable(Routes.Community.route) {
                Text(text = "Community Screen")
            }
            composable(Routes.Profile.route) {
                Text(text = "Profile Screen")
            }
        }
    }
}
