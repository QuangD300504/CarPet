package com.example.carpet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.screens.LoginScreen
import com.example.carpet.presentation.screens.MainScreen

@Composable
fun CarPetNavGraph() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.Login.route
    ) {
        // 1. Login screen with no footer
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    //go to main and delete login history
                    rootNavController.navigate("main") {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 2.Main screen with footer
        composable("main") {
            MainScreen()
        }
    }
}