package com.example.vetbook.presentation.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
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
import com.example.vetbook.utils.compressImageForAvatar
import com.example.vetbook.presentation.viewmodels.HomeViewModel
import com.example.vetbook.presentation.viewmodels.ProfileViewModel
import com.example.vetbook.presentation.viewmodels.ServiceDetailViewModel

// ==================== ANIMATION CONFIGURATION ====================

// Tab order for smart horizontal sliding
private val tabOrder = listOf(
    Routes.Home.route,
    Routes.Calendar.route,
    Routes.Store.route,
    Routes.Pet.route
)

// Modal screens that slide up from bottom
private val modalRoutes = setOf(
    Routes.AddPet.route,
    Routes.Cart.route,
    Routes.Payment.route,
    Routes.Notifications.route
)

// Animation durations
private const val ANIM_DURATION_FAST = 250
private const val ANIM_DURATION_STANDARD = 300

/**
 * Determines the direction of tab navigation
 * Returns: -1 (left), 0 (no tab change), 1 (right)
 */
private fun getTabDirection(from: String?, to: String?): Int {
    val fromIndex = tabOrder.indexOf(from)
    val toIndex = tabOrder.indexOf(to)
    return when {
        fromIndex == -1 || toIndex == -1 -> 0 // Not a tab navigation
        fromIndex < toIndex -> 1 // Moving right
        fromIndex > toIndex -> -1 // Moving left
        else -> 0 // Same tab
    }
}

/**
 * Check if route is a modal screen
 */
private fun isModalRoute(route: String?): Boolean {
    return route in modalRoutes
}

/**
 * Get base route without parameters (e.g., "doctor_profile/{id}" -> "doctor_profile")
 */
private fun getBaseRoute(route: String?): String? {
    return route?.split("/")?.firstOrNull()
}

// ==================== ANIMATION BUILDERS ====================

/**
 * Smart tab slide animation - slides left or right based on tab order
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideEnter(): EnterTransition {
    val direction = getTabDirection(
        from = initialState.destination.route,
        to = targetState.destination.route
    )
    return when (direction) {
        1 -> slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(ANIM_DURATION_FAST)
        ) + fadeIn(tween(ANIM_DURATION_FAST))
        -1 -> slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(ANIM_DURATION_FAST)
        ) + fadeIn(tween(ANIM_DURATION_FAST))
        else -> fadeIn(tween(ANIM_DURATION_FAST))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideExit(): ExitTransition {
    val direction = getTabDirection(
        from = initialState.destination.route,
        to = targetState.destination.route
    )
    return when (direction) {
        1 -> slideOutHorizontally(
            targetOffsetX = { -it / 3 }, // Parallax effect
            animationSpec = tween(ANIM_DURATION_FAST)
        ) + fadeOut(tween(ANIM_DURATION_FAST))
        -1 -> slideOutHorizontally(
            targetOffsetX = { it / 3 }, // Parallax effect
            animationSpec = tween(ANIM_DURATION_FAST)
        ) + fadeOut(tween(ANIM_DURATION_FAST))
        else -> fadeOut(tween(ANIM_DURATION_FAST))
    }
}

/**
 * Modal slide from bottom animation
 */
private fun modalSlideEnter(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(ANIM_DURATION_STANDARD)
    ) + fadeIn(tween(ANIM_DURATION_STANDARD))
}

private fun modalSlideExit(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(ANIM_DURATION_STANDARD)
    ) + fadeOut(tween(ANIM_DURATION_STANDARD))
}

private fun modalBackgroundFade(): ExitTransition {
    return fadeOut(tween(ANIM_DURATION_STANDARD / 2))
}

/**
 * Standard push/pop horizontal slide animation
 */
private fun standardPushEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(ANIM_DURATION_STANDARD)
    ) + fadeIn(tween(ANIM_DURATION_STANDARD))
}

private fun standardPushExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it / 4 }, // Slight parallax
        animationSpec = tween(ANIM_DURATION_STANDARD)
    ) + fadeOut(tween(ANIM_DURATION_STANDARD))
}

private fun standardPopEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it / 4 }, // Slight parallax
        animationSpec = tween(ANIM_DURATION_STANDARD)
    ) + fadeIn(tween(ANIM_DURATION_STANDARD))
}

private fun standardPopExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(ANIM_DURATION_STANDARD)
    ) + fadeOut(tween(ANIM_DURATION_STANDARD))
}

/**
 * Determines appropriate animations based on route context
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.getEnterTransition(): EnterTransition {
    val targetRoute = getBaseRoute(targetState.destination.route)
    val initialRoute = getBaseRoute(initialState.destination.route)
    
    return when {
        // Modal screens
        isModalRoute(targetRoute) -> modalSlideEnter()
        // Tab navigation
        targetRoute in tabOrder && initialRoute in tabOrder -> tabSlideEnter()
        // Standard navigation
        else -> standardPushEnter()
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.getExitTransition(): ExitTransition {
    val targetRoute = getBaseRoute(targetState.destination.route)
    val initialRoute = getBaseRoute(initialState.destination.route)
    
    return when {
        // Exiting to modal (background fades)
        isModalRoute(targetRoute) -> modalBackgroundFade()
        // Tab navigation
        targetRoute in tabOrder && initialRoute in tabOrder -> tabSlideExit()
        // Standard navigation
        else -> standardPushExit()
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.getPopEnterTransition(): EnterTransition {
    val targetRoute = getBaseRoute(targetState.destination.route)
    val initialRoute = getBaseRoute(initialState.destination.route)
    
    return when {
        // Returning from modal (background reappears)
        isModalRoute(initialRoute) -> fadeIn(tween(ANIM_DURATION_STANDARD))
        // Standard back navigation
        else -> standardPopEnter()
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.getPopExitTransition(): ExitTransition {
    val initialRoute = getBaseRoute(initialState.destination.route)
    
    return when {
        // Modal closing
        isModalRoute(initialRoute) -> modalSlideExit()
        // Standard back navigation
        else -> standardPopExit()
    }
}

// ==================== MAIN SCREEN ====================

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val bottomNavController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val categories by homeViewModel.categories.collectAsState()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val homeUiState by homeViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val profileImageUrl = profileUiState.user?.profileImageUrl

    var showStoreLocationDropdown by remember { mutableStateOf(false) }

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
                        profileImageUrl = profileImageUrl,
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
                currentRoute == Routes.Calendar.route -> {
                    SimpleTopBar(
                        title = "Calendar",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Services.route -> {
                    SimpleTopBar(
                        title = "Services",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Store.route -> {
                    StoreHeader(
                        currentLocation = "Ho Chi Minh City",
                        onLocationClick = { 
                            showStoreLocationDropdown = !showStoreLocationDropdown
                        },
                        onCartClick = {
                            bottomNavController.navigate(Routes.Cart.route)
                        },
                        onNotificationClick = {
                            bottomNavController.navigate(Routes.Notifications.route)
                        },
                        onProfileClick = {
                            bottomNavController.navigate(Routes.Profile.route)
                        },
                        profileImageUrl = profileImageUrl,
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
                currentRoute == Routes.Profile.route -> {
                    SimpleTopBar(
                        title = "Profile",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Cart.route -> {
                    SimpleTopBar(
                        title = "Cart",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Payment.route -> {
                    SimpleTopBar(
                        title = "Payment",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Veterinarians.route -> {
                    SimpleTopBar(
                        title = "Veterinary Care",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute?.startsWith("doctor_profile/") == true -> {
                    SimpleTopBar(
                        title = "Doctor Profile",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute?.startsWith("book_appointment/") == true -> {
                    SimpleTopBar(
                        title = "Book Appointment",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                else -> { }
            }
        },
        bottomBar = {
            val hideBottomBarRoutes = setOf(
                Routes.Veterinarians.route,
                Routes.BookAppointment.route,
                Routes.PaymentResult.route,
                Routes.Products.route,
                Routes.Cart.route,
            )

            if (currentRoute !in hideBottomBarRoutes) {
                VetBookBottomBar(navController = bottomNavController)
            }
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
            // ==================== BOTTOM NAV TABS ====================

            composable(
                route = Routes.Home.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onSeeAllClick = {
                            bottomNavController.navigate(Routes.Services.route) {
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

            composable(
                route = Routes.Pet.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    PetScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onPetClick = { petId ->
                            bottomNavController.navigate(Routes.PetProfile.createRoute(petId))
                        },
                        onCartClick = {
                            bottomNavController.navigate(Routes.Cart.route)
                        },
                        onNotificationClick = {
                            bottomNavController.navigate(Routes.Notifications.route)
                        },
                        onProfileClick = {
                            bottomNavController.navigate(Routes.Profile.route)
                        },
                        onAddPetClick = {
                            bottomNavController.navigate(Routes.AddPet.route)
                        }
                    )
                }
            }

            // ==================== SUB-SCREENS ====================

            composable(
                route = Routes.Services.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
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
                route = Routes.Calendar.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    com.example.vetbook.presentation.screens.calendar.CalendarScreen()
                }
            }

            composable(
                route = Routes.ServiceDetail.route,
                arguments = listOf(navArgument("serviceId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
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

            composable(
                route = Routes.Store.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    StoreScreen(
                        showLocationDropdown = showStoreLocationDropdown,
                        onLocationDropdownDismiss = { showStoreLocationDropdown = false },
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

            // ==================== MODAL SCREENS ====================

            composable(
                route = Routes.Notifications.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    NotificationScreen(
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }

            composable(
                route = Routes.EditProfile.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    EditProfileScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onSubmitClick = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }

            composable(
                route = Routes.Language.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    LanguageScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onLanguageSelected = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }

            composable(
                route = Routes.PrivacyPolicy.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    PrivacyPolicyScreen(
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
            }

            composable(
                route = Routes.Products.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
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

            composable(
                route = Routes.Cart.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                CartScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCheckoutClick = {
                        bottomNavController.navigate(Routes.Payment.route)
                    }
                )
            }

            composable(
                route = Routes.Payment.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                PaymentScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onCheckoutFinished = { isSuccess ->
                        bottomNavController.navigate(Routes.PaymentResult.createRoute(isSuccess)) {
                            popUpTo(Routes.Store.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Routes.Profile.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                val context = LocalContext.current
                var localAvatarUri by remember { mutableStateOf<Uri?>(null) }

                val imagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        localAvatarUri = uri
                        val bytes = compressImageForAvatar(context, uri)
                        if (bytes != null) {
                            profileViewModel.uploadAvatar(bytes) { result ->
                                result.onSuccess {
                                    // After server confirms, we can drop the temporary local override
                                       localAvatarUri = null
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onBackClick = { bottomNavController.popBackStack() },
                        avatarOverride = localAvatarUri,
                        onAvatarClick = {
                            imagePicker.launch("image/*")
                        },
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
            }

            composable(
                route = Routes.Accommodation.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                AccommodationScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onAccommodationClick = { accommodationId ->
                        // TODO: Navigate to accommodation detail screen
                    }
                )
            }

            composable(
                route = Routes.AddPet.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                com.example.vetbook.presentation.screens.pets.AddPetScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onSaved = {
                        bottomNavController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.PetProfile.route,
                arguments = listOf(navArgument("petId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                PetProfileScreen(
                    petId = petId,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable(
                route = Routes.Veterinarians.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                VeterinariansScreen(
                    onVetClick = { doctorId ->
                        bottomNavController.navigate(Routes.DoctorProfile.createRoute(doctorId))
                    }
                )
            }

            composable(
                route = Routes.DoctorProfile.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
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
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                BookAppointmentScreen(
                    doctorId = doctorId,
                    onBackClick = { bottomNavController.popBackStack() },
                    onPaymentReady = { isSuccessString ->
                        val isSuccess = isSuccessString.toBoolean()
                        bottomNavController.navigate(Routes.PaymentResult.createRoute(isSuccess)) {
                            popUpTo(Routes.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Routes.PaymentResult.route,
                arguments = listOf(navArgument("isSuccess") { type = NavType.BoolType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
                PaymentResultScreen(
                    isSuccess = isSuccess,
                    onContinueShoppingClick = {
                        bottomNavController.popBackStack(Routes.Store.route, inclusive = false)
                    },
                    onHomeClick = {
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
