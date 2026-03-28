package com.example.vetbook.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vetbook.presentation.navigation.Routes
import com.example.vetbook.presentation.theme.Brand

private data class BottomNavEntry(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavEntries = listOf(
    BottomNavEntry(Routes.Home.route,     "Home",     Icons.Default.Home),
    BottomNavEntry(Routes.Calendar.route, "Calendar", Icons.Default.CalendarMonth),
    BottomNavEntry(Routes.Store.route,    "Store",    Icons.Default.ShoppingBag),
    BottomNavEntry(Routes.Pet.route,      "Pet",      Icons.Default.Pets)
)

// Maps every sub-screen route prefix to its owning tab route.
// This lets the bar highlight the correct tab when inside a sub-screen.
private val routeToTab = mapOf(
    // Home tab owns
    Routes.Home.route         to Routes.Home.route,
    Routes.Services.route     to Routes.Home.route,
    Routes.Notifications.route to Routes.Home.route,
    Routes.Community.route    to Routes.Home.route,
    // Vet / booking flow — lives under Home tab
    Routes.Veterinarians.route to Routes.Home.route,
    "doctor_profile"          to Routes.Home.route,
    "book_appointment"        to Routes.Home.route,
    "service_detail"          to Routes.Home.route,
    "payment_result"          to Routes.Home.route,
    "in_app_payment"          to Routes.Home.route,
    // Calendar tab
    Routes.Calendar.route     to Routes.Calendar.route,
    // Store tab
    Routes.Store.route        to Routes.Store.route,
    Routes.Cart.route         to Routes.Store.route,
    Routes.Payment.route      to Routes.Store.route,
    Routes.OrderHistory.route to Routes.Store.route,
    "order_detail"            to Routes.Store.route,
    "product_detail"          to Routes.Store.route,
    "products"                to Routes.Store.route,
    // Pet tab
    Routes.Pet.route          to Routes.Pet.route,
    Routes.AddPet.route       to Routes.Pet.route,
    "pet_profile"             to Routes.Pet.route,
    "vaccination_list"        to Routes.Pet.route,
    "vaccination_detail"      to Routes.Pet.route,
    "add_vaccination"         to Routes.Pet.route,
    // Profile — treated as Home tab since it's accessed from multiple places
    Routes.Profile.route      to Routes.Home.route,
    Routes.EditProfile.route  to Routes.Home.route,
    Routes.Security.route     to Routes.Home.route,
    Routes.Language.route     to Routes.Home.route,
    Routes.PrivacyPolicy.route to Routes.Home.route,
    Routes.HelpSupport.route  to Routes.Home.route,
)

private fun resolveTab(currentRoute: String?): String? {
    if (currentRoute == null) return null
    // Try exact match first
    routeToTab[currentRoute]?.let { return it }
    // Try prefix match for parameterised routes like "doctor_profile/abc123"
    val prefix = currentRoute.substringBefore("/").substringBefore("?")
    return routeToTab[prefix]
}

@Composable
fun VetBookBottomBar(navController: NavController) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            windowInsets   = WindowInsets(0, 0, 0, 0)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val activeTab = resolveTab(currentRoute)

            bottomNavEntries.forEach { entry ->
                val isSelected = activeTab == entry.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute == entry.route) {
                            // Already on this tab's exact root — nothing to do
                            return@NavigationBarItem
                        }
                        navController.navigate(entry.route) {
                            // Pop back to the graph root without saving state.
                            // saveState/restoreState is intentionally omitted because
                            // Profile (and other sub-screens) get pushed on top of Home
                            // without their own popUpTo — saving state would capture
                            // [Home, Profile] and restore it when switching back to Home,
                            // making Profile appear in the wrong tab.
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector        = entry.icon,
                            contentDescription = entry.label,
                            tint               = if (isSelected) Brand else Color(0xFF94A3B8),
                            modifier           = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            text       = entry.label,
                            fontSize   = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (isSelected) Brand else Color(0xFF94A3B8)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor      = Brand.copy(alpha = 0.12f),
                        selectedIconColor   = Brand,
                        selectedTextColor   = Brand,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        }
    }
}