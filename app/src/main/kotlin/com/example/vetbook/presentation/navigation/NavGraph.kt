package com.example.vetbook.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetbook.presentation.screens.MainScreen
import com.example.vetbook.presentation.viewmodels.MainViewModel
import com.example.vetbook.presentation.screens.auth.*
import com.example.vetbook.presentation.viewmodels.LoginViewModel
import com.example.vetbook.presentation.viewmodels.ContinueLoginViewModel
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface

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
                        if (mainViewModel.isGoogleUser()) {
                            // Google users don't have a password — skip the password
                            // re-auth screen and go straight to the app.
                            "main"
                        } else {
                            Routes.ContinueLogin.route
                        }
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
                    // AUTH-01: After sign-up, show onboarding prompt to add first pet
                    rootNavController.navigate(Routes.Onboarding.route) {
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

        // AUTH-01: Post sign-up onboarding — prompts new users to add their first pet
        composable(Routes.Onboarding.route) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = HealthSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = HealthPrimary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                tint = HealthPrimary,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        "Thêm thú cưng của bạn!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Để theo dõi lịch tiêm chủng, đặt lịch hẹn và chăm sóc tốt hơn, hãy thêm thú cưng đầu tiên của bạn ngay nhé!",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(36.dp))
                    Button(
                        onClick = {
                            rootNavController.navigate("main") {
                                popUpTo(Routes.Onboarding.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Pets, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Thêm thú cưng ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            rootNavController.navigate("main") {
                                popUpTo(Routes.Onboarding.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bỏ qua, thêm sau", color = Color.Gray)
                    }
                }
            }
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