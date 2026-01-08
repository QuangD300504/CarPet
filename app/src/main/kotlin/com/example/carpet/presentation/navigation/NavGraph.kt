package com.example.carpet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.screens.LoginScreen
import com.example.carpet.presentation.screens.MainScreen
import com.example.carpet.presentation.screens.SignInScreen

@Composable
fun CarPetNavGraph() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.Login.route
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    // Fixed route here to match Routes.SignUp.route
                    rootNavController.navigate(Routes.SignUp.route)
                }
            )
        }

        composable(Routes.SignUp.route) {
            SignInScreen(
                onSignInSuccess = {
                    // Navigate back to login
                    rootNavController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
                    // Navigate to Login using the root NavController
                    // This clears the entire back stack and resets the session
                    rootNavController.navigate(Routes.Login.route) {
                        popUpTo(rootNavController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}