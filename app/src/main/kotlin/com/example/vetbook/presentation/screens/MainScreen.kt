package com.example.vetbook.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vetbook.presentation.components.VetBookBottomBar
import com.example.vetbook.presentation.components.topbars.HomeTopBar
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.navigation.Routes
import com.example.vetbook.presentation.screens.service_detail.ServiceDetailScreen
import com.example.vetbook.presentation.viewmodels.HomeViewModel
import com.example.vetbook.presentation.viewmodels.ServiceDetailViewModel

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val bottomNavController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val categories by homeViewModel.categories.collectAsState()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            when {
                currentRoute == Routes.Home.route -> {
                    HomeTopBar(hasNotification = homeUiState.hasNotification)
                }
                currentRoute == Routes.Service.route -> {
                    SimpleTopBar(
                        title = "Services",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Community.route -> {
                    SimpleTopBar(
                        title = "Community",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Profile.route -> {
                    SimpleTopBar(
                        title = "Profile",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Veterinarians.route -> {
                    SimpleTopBar(
                        title = "Veterinarians",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute?.startsWith("doctor_profile") == true -> {
                    SimpleTopBar(
                        title = "Doctor Profile",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                else -> { }
            }
        },
        bottomBar = {
            VetBookBottomBar(navController = bottomNavController)
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
                        handleServiceNavigation(category.id, bottomNavController)
                    }
                )
            }
            composable(Routes.Service.route) {
                ServiceScreen(
                    categories = categories,
                    onCategoryClick = { categoryId ->
                        handleServiceNavigation(categoryId, bottomNavController)
                    }
                )
            }

            composable(
                route = Routes.ServiceDetail.route,
                arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
            ) {
                val detailViewModel: ServiceDetailViewModel = hiltViewModel()
                val detailCategory by detailViewModel.category.collectAsState()
                val detailData by detailViewModel.detail.collectAsState()

                if (detailCategory != null && detailData != null) {
                    ServiceDetailScreen(
                        category = detailCategory!!,
                        detail = detailData!!,
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }
            
            composable(Routes.Community.route) {
                CommunityScreen(
                    onAdoptClick = { petId ->
                        bottomNavController.navigate(Routes.PetProfile.createRoute(petId))
                    }
                )
            }
            
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onPetClick = { petId ->
                        bottomNavController.navigate(Routes.PetProfile.createRoute(petId))
                    }
                )
            }

            composable(
                route = Routes.PetProfile.route,
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                PetProfileScreen(
                    petId = petId,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable(Routes.Veterinarians.route) {
                VeterinariansScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onVetClick = { doctorId ->
                        bottomNavController.navigate(Routes.DoctorProfile.createRoute(doctorId))
                    }
                )
            }

            composable(
                route = Routes.DoctorProfile.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                DoctorProfileScreen(
                    doctorId = doctorId,
                    onBackClick = { bottomNavController.popBackStack() },
                    onBookClick = {
                        bottomNavController.navigate(Routes.ServiceDetail.createRoute("cat_vet"))
                    }
                )
            }
        }
    }
}

private fun handleServiceNavigation(categoryId: String, navController: NavController) {
    if (categoryId == "cat_vet") {
        navController.navigate(Routes.Veterinarians.route)
    } else {
        navController.navigate(Routes.ServiceDetail.createRoute(categoryId))
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
