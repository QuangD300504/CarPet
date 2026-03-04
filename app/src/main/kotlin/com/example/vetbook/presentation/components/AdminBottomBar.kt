package com.example.vetbook.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vetbook.presentation.theme.Brand

// ── Nav item definition with Material vector icon ──────────────────────────────
private data class AdminBottomNavEntry(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// Define Admin Routes locally or in your Routes file
object AdminRoutes {
    const val DASHBOARD = "admin_dashboard"
    const val STORE = "admin_store"
    const val VET = "admin_vet"
    const val ADD_EDIT_VET = "admin_add_edit_vet/{vetId}"
    const val SERVICES = "admin_services"
    const val SETTINGS = "admin_settings"
    const val ADD_EDIT_PRODUCT = "admin_add_edit_product/{productId}"

    fun addEditProductRoute(productId: String = "new") = "admin_add_edit_product/$productId"
    fun addEditVetRoute(vetId: String = "new") = "admin_add_edit_vet/$vetId"

    const val BANNERS = "admin_banners"
    const val ADD_EDIT_BANNER = "admin_add_edit_banner/{bannerId}"
    fun addEditBannerRoute(bannerId: String = "new") = "admin_add_edit_banner/$bannerId"
}

// Ensure 5 distinct items for the Admin Navigation
private val adminBottomNavEntries = listOf(
    AdminBottomNavEntry(AdminRoutes.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
    AdminBottomNavEntry(AdminRoutes.STORE,     "Store",     Icons.Default.Storefront),
    AdminBottomNavEntry(AdminRoutes.VET,       "Vet Care",  Icons.Default.MedicalServices),
    AdminBottomNavEntry(AdminRoutes.SERVICES,  "Services",  Icons.Default.HomeRepairService),
    AdminBottomNavEntry(AdminRoutes.SETTINGS,  "Settings",  Icons.Default.Settings)
)

@Composable
fun AdminBottomBar(navController: NavController) {
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

            adminBottomNavEntries.forEach { entry ->
                val isSelected = currentRoute?.startsWith(entry.route) == true || currentRoute == entry.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
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
