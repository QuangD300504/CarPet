package com.example.carpet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.screens.HomeScreen
import com.example.carpet.presentation.screens.LoginScreen

@Composable
fun CarPetNavGraph(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Login.route){
        composable(Routes.Login.route){
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route){
                        popUpTo(Routes.Login.route){
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Routes.Home.route){
            HomeScreen()
        }
    }
}