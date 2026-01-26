package com.example.vetbook.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vetbook.presentation.navigation.bottomNavItems

@Composable
fun VetBookBottomBar(navController: NavController) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { startRoute ->
                                    popUpTo(startRoute) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            tint = if (isSelected) Color(0xFFFFE357) else Color.Black // Yellow from Figma when selected
                        )
                    },
                    label = {
                        androidx.compose.material3.Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (isSelected) Color(0xFFFFE357) else Color.Black
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFFFFE357),
                        selectedTextColor = Color.Transparent, // Custom label handles text color
                        unselectedIconColor = Color.Black,
                        unselectedTextColor = Color.Transparent // Custom label handles text color
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun VetBookBottomBarPreview() {
    val navController = rememberNavController()
    VetBookBottomBar(navController = navController)
}
