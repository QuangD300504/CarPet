package com.example.vetbook.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.vetbook.presentation.components.store.StoreHeader
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            when {
                currentRoute == Routes.Home.route -> {
                    HomeTopBar(
                        currentLocation = "Ho Chi Minh City",
                        onLocationClick = { /* TODO: open location picker */ },
                        onCartClick = {
                            bottomNavController.navigate(Routes.Store.route)
                        },
                        onNotificationClick = {
                            bottomNavController.navigate(Routes.Notifications.route)
                        },
                        onProfileClick = {
                            bottomNavController.navigate(Routes.Profile.route)
                        },
                        searchPlaceholder = "Search for a service",
                        searchValue = "",
                        onSearchChange = { /* no-op for now */ }
                    )
                }
                currentRoute == Routes.Pet.route -> {
                    SimpleTopBar(
                        title = "Pet",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Service.route -> {
                    SimpleTopBar(
                        title = "Calendar",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Store.route -> {
                    StoreHeader(
                        currentLocation = "Ho Chi Minh City",
                        onLocationClick = { /* toggle handled in screen state later */ },
                        onCartClick = {
                            bottomNavController.navigate(Routes.Cart.route)
                        },
                        onNotificationClick = {
                            bottomNavController.navigate(Routes.Notifications.route)
                        },
                        onProfileClick = {
                            bottomNavController.navigate(Routes.Profile.route)
                        },
                        showSearchBar = true,
                        searchPlaceholder = "Search for your items",
                        onSearchChange = { /* handled inside screen state for now */ },
                        searchValue = ""
                    )
                }
                currentRoute == Routes.Notifications.route -> {
                    SimpleTopBar(
                        title = "Notifications",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.EditProfile.route -> {
                    SimpleTopBar(
                        title = "Edit profile",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Language.route -> {
                    SimpleTopBar(
                        title = "Language",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.PrivacyPolicy.route -> {
                    SimpleTopBar(
                        title = "Privacy policy",
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
        val topBarPadding = innerPadding.calculateTopPadding()
        val contentModifier = Modifier.padding(
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = innerPadding.calculateBottomPadding()
        )

        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = contentModifier
        ) {
            composable(Routes.Home.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
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
            }
            composable(Routes.Pet.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    PetScreen(
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }
            composable(Routes.Service.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    ServiceScreen(
                        categories = categories,
                        onCategoryClick = { categoryId ->
                            handleServiceNavigation(categoryId, bottomNavController)
                        },
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
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
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    StoreScreen(
                        onProductsClick = {
                            bottomNavController.navigate(Routes.Products.route)
                        },
                        onCartClick = {
                            bottomNavController.navigate(Routes.Cart.route)
                        },
                        onNotificationClick = {
                            bottomNavController.navigate(Routes.Notifications.route)
                        },
                        onProfileClick = {
                            bottomNavController.navigate(Routes.Profile.route)
                        }
                    )
                }
            }
            composable(Routes.Notifications.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    NotificationScreen(
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }
            composable(Routes.EditProfile.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    EditProfileScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onSubmitClick = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }
            composable(Routes.Language.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    LanguageScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onLanguageSelected = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }
            composable(Routes.PrivacyPolicy.route) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    PrivacyPolicyScreen(
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }
            
            composable(Routes.Products.route) {
                ProductsScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCartClick = {
                        bottomNavController.navigate(Routes.Cart.route)
                    },
                    onNotificationClick = {
                        bottomNavController.navigate(Routes.Notifications.route)
                    },
                    onProfileClick = {
                        bottomNavController.navigate(Routes.Profile.route)
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
