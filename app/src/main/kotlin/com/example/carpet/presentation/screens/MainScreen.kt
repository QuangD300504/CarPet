package com.example.carpet.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.carpet.presentation.viewmodels.HomeViewModelFactory

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val repository = com.example.carpet.data.repository.MockServiceRepository()
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )
    val categories by homeViewModel.categories.collectAsState()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (currentRoute == Routes.Home.route) {
                HomeTopBar(hasNotification = homeUiState.hasNotification)
            } else {
                // Simple top bar for other screens
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
                HomeScreen(
                    viewModel = homeViewModel,
                    onSeeAllClick = {
                        bottomNavController.navigate(Routes.Service.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCategoryClick = { category ->
                        println("Clicked: ${category.title}")
                    }
                )
            }
            composable(Routes.Service.route) {
                ServiceScreen(
                    categories = categories,
                    onCategoryClick = { categoryId ->
                        println("Navigate to detail of: $categoryId")
                    }
                )
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
@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}