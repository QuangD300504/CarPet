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
import androidx.compose.material3.MaterialTheme
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
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
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
import com.example.vetbook.presentation.screens.services.ServiceDetailScreen
import com.example.vetbook.presentation.screens.home.HomeScreen
import com.example.vetbook.presentation.screens.community.CommunityScreen
import com.example.vetbook.presentation.screens.services.ServiceScreen
import com.example.vetbook.presentation.screens.profile.*
import com.example.vetbook.presentation.screens.store.*
import com.example.vetbook.presentation.screens.vetcare.*
import com.example.vetbook.presentation.screens.accommodation.AccommodationScreen
import com.example.vetbook.presentation.screens.accommodation.AccommodationDetailScreen
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.screens.pets.*
import com.example.vetbook.presentation.viewmodels.*
import com.example.vetbook.presentation.viewmodels.HomeViewModel
import com.example.vetbook.presentation.viewmodels.ProfileViewModel
import com.example.vetbook.presentation.viewmodels.ServiceDetailViewModel
import com.example.vetbook.presentation.viewmodels.SharedNotificationViewModel
import com.example.vetbook.utils.compressImageForAvatar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val bottomNavController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val storeViewModel: StoreViewModel = hiltViewModel()
    val sharedNotifViewModel: SharedNotificationViewModel = hiltViewModel()
    val veterinariansViewModel: VeterinariansViewModel = hiltViewModel()
    val vaccinationViewModel: VaccinationViewModel = hiltViewModel()
    var pendingVaccineId by remember { mutableStateOf<String?>(null) }
    val categories by homeViewModel.categories.collectAsState()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val profileImageUrl = profileUiState.user?.profileImageUrl
    val homeSearchQuery by homeViewModel.searchQuery.collectAsState()
    val hasUnread by sharedNotifViewModel.hasUnread.collectAsState()
    val currentUserId = profileUiState.user?.id

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            sharedNotifViewModel.startListening(currentUserId)
        }
    }

    var showStoreLocationDropdown by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { com.example.vetbook.presentation.components.common.VetBookSnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            when {
                currentRoute == Routes.Home.route -> {
                    HomeTopBar(
                        currentLocation = "Ho Chi Minh City",
                        onLocationClick = { 
                            // In a real app, this would open a Google Maps / TomTom picker
                            // For now, we'll show a snackbar
                            scope.launch {
                                com.example.vetbook.presentation.components.common.VetBookSnackbar.show(
                                    snackbarHostState,
                                    "Bộ chọn vị trí đang được phát triển",
                                    com.example.vetbook.presentation.components.common.SnackbarType.Info
                                )
                            }
                        },
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
                        searchValue = homeSearchQuery,
                        onSearchChange = { homeViewModel.setSearch(it) },
                        hasUnreadNotifications = hasUnread
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
                        onBackClick = null,
                        profileImageUrl = profileImageUrl,
                        showSearchBar = true,
                        searchPlaceholder = "Search for your items",
                        onSearchChange = { storeViewModel.setSearchQuery(it) },
                        searchValue = storeViewModel.uiState.value.searchQuery,
                        hasUnreadNotifications = hasUnread
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
                        title       = "My Cart",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                currentRoute == Routes.Payment.route -> {
                    SimpleTopBar(
                        title       = "Checkout",
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                }
                // Note: Veterinarians, doctor_profile, book_appointment handle
                // their own headers internally (Type B / Type C pattern)
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
                "in_app_payment?url={url}",
                Routes.VaccinationList.route,
                Routes.AddVaccination.route,
                Routes.VaccinationDetail.route,
                Routes.ProductDetail.route,
                Routes.OrderHistory.route,
                Routes.Security.route,
                Routes.HelpSupport.route,
                Routes.OrderDetail.route
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
                        userName = profileUiState.user?.name,
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
                    com.example.vetbook.presentation.screens.calendar.CalendarScreen(
                        veterinariansViewModel = veterinariansViewModel,
                        onSubmitReview = { appointmentId, doctorId, rating, comment ->
                            val userName = profileUiState.user?.name ?: "Người dùng"
                            val review = com.example.vetbook.domain.models.DoctorReview(
                                id = "",
                                appointmentId = appointmentId,
                                doctorId = doctorId,
                                userId = currentUserId ?: "",
                                userName = userName,
                                rating = rating,
                                comment = comment,
                                createdAt = System.currentTimeMillis()
                            )
                            veterinariansViewModel.submitReview(review)
                        },
                        onContinuePayment = { url ->
                            bottomNavController.navigate(Routes.InAppPayment.createRoute(url))
                        }
                    )
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

                Box(modifier = Modifier.padding(top = topBarPadding).fillMaxSize()) {
                    if (detailCategory != null && detailData != null) {
                        ServiceDetailScreen(
                            category = detailCategory!!,
                            detail = detailData!!,
                            onBackClick = { bottomNavController.popBackStack() }
                        )
                    } else {
                        // Show loading or error
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = HealthPrimary
                        )
                    }
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
                        showHeader = false,
                        onProductsClick = {
                            bottomNavController.navigate(Routes.Products.route)
                        },
                        onCategoryClick = { category ->
                            bottomNavController.navigate(Routes.Products.createRoute(category))
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
                        viewModel = profileViewModel,  
                        onBackClick = { bottomNavController.popBackStack() },
                        onSubmitClick = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }

            composable(
    route = Routes.Security.route,
    enterTransition = { getEnterTransition() },
    exitTransition = { getExitTransition() },
    popEnterTransition = { getPopEnterTransition() },
    popExitTransition = { getPopExitTransition() }
) {
    Box(modifier = Modifier.padding(top = topBarPadding)) {
        SecurityScreen(
            onBackClick = { bottomNavController.popBackStack() },
            // onAccountDeleted = {
            //     mainViewModel.signOut {
            //         rootNavController.navigate(Routes.Login.route) {
            //             popUpTo(0) { inclusive = true }
            //         }
            //     }
            // }
            onAccountDeleted = { onLogout() }
        )
    }
}

            composable(Routes.HelpSupport.route) {
    HelpSupportScreen(
        onBackClick = { bottomNavController.popBackStack() }
    )
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

            @Suppress("DEPRECATION")
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
                arguments = listOf(navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                ProductsScreen(
                    viewModel = storeViewModel,
                    category = category,
                    onBackClick = { bottomNavController.popBackStack() },
                    onCartClick = {
                        bottomNavController.navigate(Routes.Cart.route)
                    },
                    onNotificationClick = {
                        bottomNavController.navigate(Routes.Notifications.route)
                    },
                    onProfileClick = {
                        bottomNavController.navigate(Routes.Profile.route)
                    },
                    onProductClick = { productId ->
                        bottomNavController.navigate(Routes.ProductDetail.createRoute(productId))
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
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    CartScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onCheckoutClick = {
                            bottomNavController.navigate(Routes.Payment.route)
                        },
                        onProductClick = { productId ->
                            bottomNavController.navigate(Routes.ProductDetail.createRoute(productId))
                        },
                        onOrderHistoryClick = {
                            bottomNavController.navigate(Routes.OrderHistory.route)
                        }
                    )
                }
            }
            composable(
                route = Routes.Payment.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                Box(modifier = Modifier.padding(top = topBarPadding)) {
                    PaymentScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onCheckoutFinished = { isSuccess ->
                            bottomNavController.navigate(Routes.PaymentResult.createRoute(isSuccess)) {
                                popUpTo(Routes.Store.route) { inclusive = false }
                            }
                        },
                        onShowPayment = { url ->
                            bottomNavController.navigate(Routes.InAppPayment.createRoute(url))
                        }
                    )
                }
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
                        onSecurityClick = {
                            bottomNavController.navigate(Routes.Security.route)
                        },
                        onHelpAndSupportClick = {
                            bottomNavController.navigate(Routes.HelpSupport.route)
                        },
                        onContactUsClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@carpetapp.com")
                                putExtra(Intent.EXTRA_SUBJECT, "CarPet Support")
                            }
                            context.startActivity(Intent.createChooser(intent, "Contact Us"))
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
                        bottomNavController.navigate(Routes.AccommodationDetail.createRoute(accommodationId))
                    }
                )
            }

            composable(
                route = Routes.AccommodationDetail.route,
                arguments = listOf(navArgument("accommodationId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val accommodationId = backStackEntry.arguments?.getString("accommodationId") ?: ""
                AccommodationDetailScreen(
                    accommodationId = accommodationId,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable(
                route = Routes.AddPet.route,
                arguments = listOf(navArgument("petId") {
    type = NavType.StringType
    nullable = true
    defaultValue = null
                }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                com.example.vetbook.presentation.screens.pets.AddPetScreen(
                    petId = petId,
                    onBackClick = { bottomNavController.popBackStack() },
                    onSaved = { isEdit ->
                        bottomNavController.popBackStack()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isEdit) "Cập nhật thú cưng thành công!" else "Thêm thú cưng thành công!"
                            )
                        }
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
                    onBackClick = { bottomNavController.popBackStack() },
                    onEditClick = { id ->
                        bottomNavController.navigate(Routes.AddPet.createRoute(id))
                    },
                    onDeleted = {
                        bottomNavController.popBackStack()
                        scope.launch {
                            snackbarHostState.showSnackbar("Đã xóa thú cưng thành công")
                        }
                    },
                    onVaccinationsViewAll = { id, name, petType, birthDate ->
                        bottomNavController.navigate(Routes.VaccinationList.createRoute(id, name, petType, birthDate?.toEpochMilli()))
                    },
                    onVaccinationClick = { vaccinationId ->
                        bottomNavController.navigate(Routes.VaccinationDetail.createRoute(vaccinationId))
                    }
                )
            }

            composable(
                route = Routes.VaccinationList.route,
                arguments = listOf(
    navArgument("petId") { type = NavType.StringType },
    navArgument("petName") { type = NavType.StringType },
    navArgument("petType") { type = NavType.StringType; defaultValue = "" },
    navArgument("birthDate") { type = NavType.StringType; defaultValue = "" }
),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                val petName = backStackEntry.arguments?.getString("petName") ?: ""
                val petType = backStackEntry.arguments?.getString("petType") ?: ""
                val birthDateStr = backStackEntry.arguments?.getString("birthDate") ?: ""
                val birthDate: java.time.Instant? = if (birthDateStr.isNotBlank()) {
                    birthDateStr.toLongOrNull()?.let { java.time.Instant.ofEpochMilli(it) }
                } else null
//                var pendingVaccineId by remember { mutableStateOf<String?>(null) }
                val viewModel: com.example.vetbook.presentation.viewmodels.VaccinationViewModel = hiltViewModel()
                com.example.vetbook.presentation.screens.pets.VaccinationListScreen(
                    petId = petId,
                    petName = petName,
                    petType = petType,
                    birthDate = birthDate,
                    viewModel = viewModel,
                    onBookAppointment = { vaccinationId ->
    pendingVaccineId = vaccinationId
    bottomNavController.navigate(Routes.Veterinarians.route)
},
                    onBackClick = { bottomNavController.popBackStack() },
                    onAddClick = {
                        bottomNavController.navigate(Routes.AddVaccination.createRoute(petId, petName))
                    },
                    onVaccinationClick = { vaccinationId ->
                        bottomNavController.navigate(Routes.VaccinationDetail.createRoute(vaccinationId))
                    }
                )
            }

            composable(
                route = Routes.AddVaccination.route,
                arguments = listOf(
                    navArgument("petId") { type = NavType.StringType },
                    navArgument("petName") { type = NavType.StringType }
                ),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId") ?: ""
                val petName = backStackEntry.arguments?.getString("petName") ?: ""
                com.example.vetbook.presentation.screens.pets.AddVaccinationScreen(
                    petId = petId,
                    petName = petName,
                    onBackClick = { bottomNavController.popBackStack() },
                    onSaved = {
                        bottomNavController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.VaccinationDetail.route,
                arguments = listOf(navArgument("vaccinationId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val vaccinationId = backStackEntry.arguments?.getString("vaccinationId") ?: ""
                com.example.vetbook.presentation.screens.pets.VaccinationDetailScreen(
                    vaccinationId = vaccinationId,
                    onBackClick = { bottomNavController.popBackStack() },
                    onVetClick = { doctorId ->
                        bottomNavController.navigate(Routes.DoctorProfile.createRoute(doctorId))
                    },
                    onBookAppointment = { vaccinationId, doctorId ->
    pendingVaccineId = vaccinationId
    if (doctorId.isBlank()) {
        bottomNavController.navigate(Routes.Veterinarians.route)
    } else {
        bottomNavController.navigate(Routes.BookAppointment.createRoute(doctorId))
    }
}
                )
            }

            composable(
                route = Routes.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                com.example.vetbook.presentation.screens.store.ProductDetailScreen(
                    productId = productId,
                    onBackClick = { bottomNavController.popBackStack() },
                    onNavigateToCart = {
                        bottomNavController.navigate(Routes.Cart.route)
                    }
                )
            }

            composable(
                route = Routes.OrderHistory.route,
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) {
                com.example.vetbook.presentation.screens.store.OrderHistoryScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onOrderClick = { orderId ->
                        bottomNavController.navigate(Routes.OrderDetail.createRoute(orderId))
                    }
                )
            }

            composable(
                route = Routes.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.example.vetbook.presentation.screens.store.OrderDetailScreen(
                    orderId = orderId,
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
                    onBackClick = { bottomNavController.popBackStack() },
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
                    onShowPayment = { url ->
                        bottomNavController.navigate(Routes.InAppPayment.createRoute(url))
                    },
                    onPaymentFinished = { isSuccess, appointmentId, vetName, appointmentAt ->
    if (isSuccess) {
        pendingVaccineId?.let { vaccId ->
            vaccinationViewModel.linkAppointment(
                vaccinationId = vaccId,
                appointmentId = appointmentId,
                scheduledDate = appointmentAt,
                vetName = vetName,
                clinicName = null
            )
        }
    }
    pendingVaccineId = null
    bottomNavController.navigate(
        Routes.PaymentResult.createRoute(isSuccess, source = "vet")
    ) {
        popUpTo(Routes.Home.route) { inclusive = false }
    }
}
                )
            }

            composable(
                route = Routes.InAppPayment.route,
                arguments = listOf(navArgument("url") { type = NavType.StringType }),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: ""
                InAppPaymentScreen(
                    url = url,
                    onBack = { bottomNavController.popBackStack() }
                )
            }

            composable(
                route = Routes.PaymentResult.route,
                arguments = listOf(
                    navArgument("isSuccess") { type = NavType.BoolType },
                    navArgument("source") {
                        type = NavType.StringType
                        defaultValue = "store"
                    }
                ),
                enterTransition = { getEnterTransition() },
                exitTransition = { getExitTransition() },
                popEnterTransition = { getPopEnterTransition() },
                popExitTransition = { getPopExitTransition() }
            ) { backStackEntry ->
                val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
                val source = backStackEntry.arguments?.getString("source") ?: "store"
                val isVetFlow = source == "vet"
                PaymentResultScreen(
                    isSuccess = isSuccess,
                    onContinueShoppingClick = {
                        bottomNavController.popBackStack(Routes.Store.route, inclusive = false)
                    },
                    onHomeClick = {
                        bottomNavController.popBackStack(Routes.Home.route, inclusive = false)
                    },
                    onViewCalendarClick = if (isVetFlow && isSuccess) {
                        {
                            // Navigate to Calendar tab, clearing the result + booking stack
                            bottomNavController.navigate(Routes.Calendar.route) {
                                popUpTo(Routes.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    } else null,
                    onTryAgainClick = if (isVetFlow && !isSuccess) {
                        {
                            // Pop back to the BookAppointment screen so user can retry
                            bottomNavController.popBackStack()
                        }
                    } else null
                )
            }
        }
    }
}

private fun handleServiceNavigation(categoryId: String, navController: NavController) {
    when (categoryId) {
        "cat_vet" -> navController.navigate(Routes.Veterinarians.route)
        "cat_shop" -> {
            navController.navigate(Routes.Store.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        else -> navController.navigate(Routes.ServiceDetail.createRoute(categoryId))
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}

