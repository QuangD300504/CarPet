package com.example.carpet.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.components.CarPetBottomBar
import com.example.carpet.presentation.navigation.Routes

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()

    Scaffold(
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
                HomeScreen()
            }
            composable(Routes.Service.route) {
                // Create this screen later
                Text(text = "Service Screen")
            }
            composable(Routes.Community.route) {
                // Create this screen later
                Text(text = "Community Screen")
            }
            composable(Routes.Profile.route) {
                // Create this screen later
                Text(text = "Profile Screen")
            }
        }
    }
}