package com.example.carpet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.screens.*

@Composable
fun VetBookNavGraph() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onAnimationFinished = {
                    rootNavController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    rootNavController.navigate(Routes.SignUp.route)
                },
                onForgotPasswordClick = {
                    rootNavController.navigate(Routes.ForgotPassword.route)
                }
            )
        }

        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = {
                    rootNavController.popBackStack()
                }
            )
        }

        composable(Routes.SignUp.route) {
            SignUpScreen(
                onLoginClick = {
                    rootNavController.popBackStack()
                },
                onSignUpComplete = {
                    rootNavController.navigate("main") {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
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
