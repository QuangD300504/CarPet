package com.example.carpet.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.carpet.presentation.components.AppointmentCard
import com.example.carpet.presentation.components.FeaturedBanner
import com.example.carpet.presentation.components.RecommendedServices
import com.example.carpet.presentation.viewmodels.HomeUiState

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item { FeaturedBanner() }
        item { AppointmentCard() }
        item { RecommendedServices(onSeeAllClick = onSeeAllClick) }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(uiState = HomeUiState())
}
