package com.example.vetbook.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vetbook.presentation.navigation.Routes
import com.example.vetbook.presentation.theme.Brand

// ── Nav item definition with Material vector icon ──────────────────────────────
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

@Composable
fun VetBookBottomBar(navController: NavController) {
    Surface(
        color           = Color.White,
        shadowElevation = 10.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            windowInsets   = WindowInsets(0, 0, 0, 0)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            bottomNavEntries.forEach { entry ->
                val isSelected = currentRoute == entry.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != entry.route) {
                            navController.navigate(entry.route) {
                                navController.graph.startDestinationRoute?.let { startRoute ->
                                    popUpTo(startRoute) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
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

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun VetBookBottomBarPreview() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Home.route) {
        composable(Routes.Home.route) {}
        composable(Routes.Calendar.route) {}
        composable(Routes.Store.route) {}
        composable(Routes.Pet.route) {}
    }
    VetBookBottomBar(navController = navController)
}
