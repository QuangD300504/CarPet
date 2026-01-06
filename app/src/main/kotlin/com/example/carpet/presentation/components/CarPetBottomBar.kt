package com.example.carpet.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource // Import this to use drawable resources
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.navigation.bottomNavItems

@Composable
fun CarPetBottomBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { startRoute ->
                            popUpTo(startRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
//                label = { Text(text = item.label) }, //cause already label in svg image
                icon = {
                    Icon(
                        // Use painterResource for your SVG drawables
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                }
            )
        }
    }
}
@Preview
@Composable
fun CarPetBottomBarPreview() {
    val navController = rememberNavController()
    CarPetBottomBar(navController = navController)
}
