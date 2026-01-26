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
import com.example.vetbook.presentation.screens.profile.*
import com.example.vetbook.presentation.screens.store.*
import com.example.vetbook.presentation.screens.vetcare.*
import com.example.vetbook.presentation.screens.accommodation.AccommodationScreen
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
                    HomeTopBar(
                        hasNotification = homeUiState.hasNotification,
                        onNotificationClick = {
                            bottomNavController.navigate(Routes.Notifications.route)
                        },
                        onProfileClick = {
                            bottomNavController.navigate(Routes.Profile.route)
                        }
                    )
                }
                // Profile route has its own yellow header built-in, don't show duplicate topBar
                // Service route now has its own header built-in
                // Veterinarians route has its own yellow header built-in
                // DoctorProfile route has its own header with back button overlay
                // BookAppointment route has its own header with back button overlay
                // Store/Products/Cart routes have their own header built-in
                // Notifications/EditProfile/Language/PrivacyPolicy routes have their own yellow headers
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
                    },
                    onBackClick = { bottomNavController.popBackStack() }
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
            
            composable(Routes.Store.route) {
                StoreScreen(
                    onProductsClick = {
                        bottomNavController.navigate(Routes.Products.route)
                    },
                    onCartClick = {
                        bottomNavController.navigate(Routes.Cart.route)
                    }
                )
            }
            
            composable(Routes.Products.route) {
                ProductsScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCartClick = {
                        bottomNavController.navigate(Routes.Cart.route)
                    }
                )
            }
            
            composable(Routes.Cart.route) {
                CartScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCheckoutClick = {
                        bottomNavController.navigate(Routes.Payment.route)
                    }
                )
            }
            
            composable(Routes.Payment.route) {
                PaymentScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCheckoutClick = {
                        // Handle final checkout
                        bottomNavController.popBackStack(Routes.Home.route, inclusive = false)
                    }
                )
            }
            
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onEditProfileClick = {
                        bottomNavController.navigate(Routes.EditProfile.route)
                    },
                    onNotificationClick = {
                        bottomNavController.navigate(Routes.Notifications.route)
                    },
                    onLanguageClick = {
                        bottomNavController.navigate(Routes.Language.route)
                    },
                    onContactUsClick = {
                        // Handle contact us
                    },
                    onPrivacyPolicyClick = {
                        bottomNavController.navigate(Routes.PrivacyPolicy.route)
                    },
                    onLogout = onLogout
                )
            }
            
            composable(Routes.Notifications.route) {
                NotificationScreen(
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }
            
            composable(Routes.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onSubmitClick = {
                        bottomNavController.popBackStack()
                    }
                )
            }
            
            composable(Routes.Language.route) {
                LanguageScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onLanguageSelected = {
                        bottomNavController.popBackStack()
                    }
                )
            }
            
            composable(Routes.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }
            
            composable(Routes.Accommodation.route) {
                AccommodationScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onAccommodationClick = { accommodationId ->
                        // TODO: Navigate to accommodation detail screen
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
                        bottomNavController.navigate(Routes.BookAppointment.createRoute(doctorId))
                    }
                )
            }
            
            composable(
                route = Routes.BookAppointment.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                BookAppointmentScreen(
                    doctorId = doctorId,
                    onBackClick = { bottomNavController.popBackStack() },
                    onConfirmClick = {
                        // Handle appointment confirmation
                        bottomNavController.popBackStack(Routes.Home.route, inclusive = false)
                    }
                )
            }
        }
    }
}

private fun handleServiceNavigation(categoryId: String, navController: NavController) {
    when (categoryId) {
        "cat_vet" -> navController.navigate(Routes.Veterinarians.route)
        "cat_hotel" -> navController.navigate(Routes.Accommodation.route)
        else -> navController.navigate(Routes.ServiceDetail.createRoute(categoryId))
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
