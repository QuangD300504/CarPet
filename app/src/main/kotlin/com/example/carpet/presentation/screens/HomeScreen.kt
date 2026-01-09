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
import com.example.carpet.domain.models.ServiceCategory
import com.example.carpet.presentation.components.AppointmentCard
import com.example.carpet.presentation.components.FeaturedBanner
import com.example.carpet.presentation.components.RecommendedServices
import com.example.carpet.presentation.models.HomeUiState
import com.example.carpet.presentation.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSeeAllClick: () -> Unit,
    onCategoryClick: (ServiceCategory) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    HomeContent(
        uiState = uiState,
        categories = categories,
        onCategoryClick = onCategoryClick,
        onSeeAllClick = onSeeAllClick
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    categories: List<ServiceCategory>,
    onCategoryClick: (ServiceCategory) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item { FeaturedBanner() }
        item { AppointmentCard() }
        item {
            RecommendedServices(
                categories = categories,
                onCategoryClick = onCategoryClick,
                onSeeAllClick = onSeeAllClick
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HomeScreenPreview() {
    HomeContent(
        uiState = HomeUiState(),
        categories = emptyList(),
        onCategoryClick = {},
        onSeeAllClick = {}
    )
}
