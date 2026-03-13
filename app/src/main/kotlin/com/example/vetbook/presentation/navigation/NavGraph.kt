package com.example.vetbook.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetbook.presentation.screens.MainScreen
import com.example.vetbook.presentation.viewmodels.MainViewModel
import com.example.vetbook.presentation.screens.auth.*
import com.example.vetbook.presentation.viewmodels.LoginViewModel
import com.example.vetbook.presentation.viewmodels.ContinueLoginViewModel

@Composable
fun VetBookNavGraph() {
    val rootNavController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onAnimationFinished = {
                    val destination = if (mainViewModel.isUserLoggedIn()) {
                        Routes.ContinueLogin.route
                    } else {
                        Routes.Login.route
                    }

                    rootNavController.navigate(destination) {
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
                    mainViewModel.signOut {
                        rootNavController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Continue Login Flow
        composable(Routes.ContinueLogin.route) {
            ContinueLoginScreen(
                onNext = {
                    rootNavController.navigate(Routes.ContinueLoginStart.route)
                }
            )
        }

        composable(Routes.ContinueLoginStart.route) {
            ContinueLoginStartScreen(
                onNext = {
                    rootNavController.navigate(Routes.ContinueLoginPassword.route)
                }
            )
        }

        composable(Routes.ContinueLoginPassword.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val continueLoginViewModel: ContinueLoginViewModel = hiltViewModel()
            ContinueLoginPasswordScreen(
                loginViewModel = loginViewModel,
                continueLoginViewModel = continueLoginViewModel,
                onForgotPasswordClick = {
                    rootNavController.navigate(Routes.ForgotPassword.route)
                },
                onLoginSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onUseAnotherAccount = {
                    rootNavController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLoginClick = { password ->
                    val email = continueLoginViewModel.uiState.value.email
                    loginViewModel.onUsernameChange(email)
                    loginViewModel.onPasswordChange(password)
                    loginViewModel.onLoginClick()
                }
            )
        }
    }
}
