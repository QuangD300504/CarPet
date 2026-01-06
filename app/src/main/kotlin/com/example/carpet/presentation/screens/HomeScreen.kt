package com.example.carpet.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carpet.presentation.components.AppointmentCard
import com.example.carpet.presentation.components.CarPetBottomBar
import com.example.carpet.presentation.components.FeaturedBanner
import com.example.carpet.presentation.components.RecommendedServices
import com.example.carpet.presentation.components.topbars.HomeTopBar
import com.example.carpet.presentation.viewmodels.HomeUiState
import com.example.carpet.presentation.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { HomeTopBar(hasNotification = uiState.hasNotification) },
        bottomBar = { CarPetBottomBar(navController = navController) }
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { FeaturedBanner() }
        item { AppointmentCard() }
        item { RecommendedServices() }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
