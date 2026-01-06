package com.example.carpet.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carpet.presentation.components.AppointmentCard
import com.example.carpet.presentation.components.FeaturedBanner
import com.example.carpet.presentation.components.HomeTopBar
import com.example.carpet.presentation.components.RecommendedServices
import com.example.carpet.presentation.viewmodels.HomeUiState
import com.example.carpet.presentation.viewmodels.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()){
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(uiState = uiState)
}
@Composable
fun HomeContent(uiState: HomeUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(80.dp)
    ) {
        item { HomeTopBar(hasNotification = uiState.hasNotification) }
        item { FeaturedBanner() }
        item { AppointmentCard() }
        item { RecommendedServices() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HomeScreenPreview(){
    HomeContent(
        uiState = HomeUiState(
            hasNotification = true
        )
    )
}

