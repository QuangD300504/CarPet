package com.example.vetbook.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vetbook.presentation.components.AdminBottomBar
import com.example.vetbook.presentation.components.AdminRoutes
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.screens.admin.AdminAddEditBannerScreen
import com.example.vetbook.presentation.screens.admin.AdminAddEditProductScreen
import com.example.vetbook.presentation.screens.admin.AdminAddEditVetScreen
import com.example.vetbook.presentation.screens.admin.AdminAppointmentsScreen
import com.example.vetbook.presentation.screens.admin.AdminBannersScreen
import com.example.vetbook.presentation.screens.admin.AdminOrdersScreen
import com.example.vetbook.presentation.screens.admin.AdminServicesScreen
import com.example.vetbook.presentation.screens.admin.AdminStoreScreen
import com.example.vetbook.presentation.screens.admin.AdminVetScreen

@Composable
fun AdminMainScreen(onLogout: () -> Unit) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Top-level tab routes (show top bar + bottom bar)
    val mainTabs = listOf(
        AdminRoutes.DASHBOARD,
        AdminRoutes.STORE,
        AdminRoutes.VET,
        AdminRoutes.SERVICES,
        AdminRoutes.SETTINGS
    )
    val isMainTab = currentRoute in mainTabs

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (isMainTab) {
                when (currentRoute) {
                    AdminRoutes.DASHBOARD -> SimpleTopBar(title = "Admin Dashboard", onBackClick = null)
                    AdminRoutes.STORE     -> SimpleTopBar(title = "Manage Store",    onBackClick = null)
                    AdminRoutes.VET       -> SimpleTopBar(title = "Manage Vet Care", onBackClick = null)
                    AdminRoutes.SERVICES  -> SimpleTopBar(title = "Services",        onBackClick = null)
                    AdminRoutes.SETTINGS  -> SimpleTopBar(title = "Settings",        onBackClick = null)
                }
            }
        },
        bottomBar = {
            if (isMainTab) AdminBottomBar(navController = bottomNavController)
        }
    ) { inner ->
        val topPad = inner.calculateTopPadding()
        val contentMod = Modifier.padding(
            start  = inner.calculateStartPadding(LayoutDirection.Ltr),
            end    = inner.calculateEndPadding(LayoutDirection.Ltr),
            bottom = inner.calculateBottomPadding()
        )

        NavHost(
            navController    = bottomNavController,
            startDestination = AdminRoutes.DASHBOARD,
            modifier         = contentMod
        ) {
            // ── DASHBOARD ────────────────────────────────────────────────────
            composable(AdminRoutes.DASHBOARD) {
                AdminDashboardTab(
                    topPad  = topPad,
                    navCtrl = bottomNavController
                )
            }

            // ── STORE ────────────────────────────────────────────────────────
            composable(AdminRoutes.STORE) {
                Column(Modifier.padding(top = topPad).fillMaxSize()) {
                    // Sub-tabs: Products | Orders
                    var tab by remember { mutableStateOf(0) }
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Products") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Orders") })
                    }
                    if (tab == 0) {
                        AdminStoreScreen(
                            onAddProductClick  = { bottomNavController.navigate(AdminRoutes.addEditProductRoute("new")) },
                            onEditProductClick = { id -> bottomNavController.navigate(AdminRoutes.addEditProductRoute(id)) }
                        )
                    } else {
                        AdminOrdersScreen()
                    }
                }
            }

            composable(AdminRoutes.ADD_EDIT_PRODUCT) {
                AdminAddEditProductScreen(
                    onBackClick            = { bottomNavController.popBackStack() },
                    onNavigateBackAfterSave = { bottomNavController.popBackStack() }
                )
            }

            // ── VET ──────────────────────────────────────────────────────────
            composable(AdminRoutes.VET) {
                Column(Modifier.padding(top = topPad).fillMaxSize()) {
                    var tab by remember { mutableStateOf(0) }
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Vets") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Appointments") })
                    }
                    if (tab == 0) {
                        AdminVetScreen(
                            onAddVetClick  = { bottomNavController.navigate(AdminRoutes.addEditVetRoute("new")) },
                            onEditVetClick = { id -> bottomNavController.navigate(AdminRoutes.addEditVetRoute(id)) }
                        )
                    } else {
                        AdminAppointmentsScreen()
                    }
                }
            }

            composable(AdminRoutes.ADD_EDIT_VET) {
                AdminAddEditVetScreen(
                    onBackClick            = { bottomNavController.popBackStack() },
                    onNavigateBackAfterSave = { bottomNavController.popBackStack() }
                )
            }

            // ── SERVICES ─────────────────────────────────────────────────────
            composable(AdminRoutes.SERVICES) {
                Box(Modifier.padding(top = topPad).fillMaxSize()) {
                    AdminServicesScreen()
                }
            }

            // ── SETTINGS ─────────────────────────────────────────────────────
            composable(AdminRoutes.SETTINGS) {
                Column(
                    Modifier.padding(top = topPad).fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Admin Settings", style = MaterialTheme.typography.titleLarge)

                    // Banners management
                    OutlinedButton(
                        onClick = { bottomNavController.navigate(AdminRoutes.BANNERS) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🏞  Manage Banners & Sponsors") }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Logout") }
                }
            }

            // ── BANNERS ──────────────────────────────────────────────────────
            composable(AdminRoutes.BANNERS) {
                AdminBannersScreen(
                    onAddBannerClick  = { bottomNavController.navigate(AdminRoutes.addEditBannerRoute("new")) },
                    onEditBannerClick = { id -> bottomNavController.navigate(AdminRoutes.addEditBannerRoute(id)) }
                )
            }

            composable(AdminRoutes.ADD_EDIT_BANNER) {
                AdminAddEditBannerScreen(
                    onBackClick            = { bottomNavController.popBackStack() },
                    onNavigateBackAfterSave = { bottomNavController.popBackStack() }
                )
            }
        }
    }
}

/** Simple dashboard tab – shows shortcut cards for each section */
@Composable
private fun AdminDashboardTab(
    topPad: androidx.compose.ui.unit.Dp,
    navCtrl: androidx.navigation.NavController
) {
    Column(
        Modifier.padding(top = topPad).fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Quick Access", style = MaterialTheme.typography.titleMedium)
        val items = listOf(
            "🛒  Store & Orders"   to AdminRoutes.STORE,
            "🐾  Vet Care"         to AdminRoutes.VET,
            "📋  Services"         to AdminRoutes.SERVICES,
            "🏞  Banners"          to AdminRoutes.BANNERS,
            "⚙️  Settings"         to AdminRoutes.SETTINGS
        )
        items.forEach { (label, route) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick  = { navCtrl.navigate(route) },
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text     = label,
                    modifier = Modifier.padding(16.dp),
                    style    = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
