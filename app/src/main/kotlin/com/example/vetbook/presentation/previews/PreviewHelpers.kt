package com.example.vetbook.presentation.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetbook.presentation.components.VetBookBottomBar
import com.example.vetbook.presentation.navigation.Routes

/**
 * Lightweight scaffold used only for Previews.
 * It sets up a minimal NavHost so VetBookBottomBar has a graph to read the current route from.
 */
@Composable
fun PreviewNavScaffold(
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val navController = rememberNavController()

    // Minimal graph to keep bottom bar happy during previews
    Box(modifier = Modifier.size(0.dp)) {
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route
        ) {
            composable(Routes.Home.route) {}
            composable(Routes.Service.route) {}
            composable(Routes.Store.route) {}
            composable(Routes.Profile.route) {}
            composable(Routes.Pet.route) {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = topBar,
        bottomBar = { VetBookBottomBar(navController) }
    ) { padding ->
        content(padding)
    }
}
