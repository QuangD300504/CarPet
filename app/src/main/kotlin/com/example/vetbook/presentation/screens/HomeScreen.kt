package com.example.vetbook.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.presentation.components.CaringBanner
import com.example.vetbook.presentation.components.RecommendedServices
import com.example.vetbook.presentation.components.ServiceCategoriesSection
import com.example.vetbook.presentation.components.SponsoredSection
import com.example.vetbook.presentation.models.HomeUiState
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.components.topbars.HomeTopBar
import com.example.vetbook.presentation.viewmodels.HomeViewModel

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
        item { CaringBanner() }
        
        item { 
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            ServiceCategoriesSection(
                categories = categories,
                onCategoryClick = onCategoryClick,
                onViewAllClick = onSeeAllClick
            )
        }
        
        item {
            RecommendedServices(
                categories = categories,
                onCategoryClick = onCategoryClick,
                onSeeAllClick = onSeeAllClick
            )
        }
        
        item {
            SponsoredSection()
        }
        
        item {
            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "That's all for now!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun previewHomeCategories() = listOf(
    ServiceCategory("cat_vet", "Vet Care", "Book vets", com.example.vetbook.R.drawable.services),
    ServiceCategory("cat_hotel", "Pet Hotel", "Stay safe", com.example.vetbook.R.drawable.services),
    ServiceCategory("cat_walk", "Dog Walk", "Walk time", com.example.vetbook.R.drawable.services),
    ServiceCategory("cat_shop", "Shop", "Buy goods", com.example.vetbook.R.drawable.store)
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HomeScreenPreview() {
    PreviewNavScaffold(
        topBar = {
            HomeTopBar(
                currentLocation = "Ho Chi Minh City",
                onLocationClick = {},
                onCartClick = {},
                onNotificationClick = {},
                onProfileClick = {},
                searchPlaceholder = "Search for a service",
                searchValue = "",
                onSearchChange = {}
            )
        }
    ) { padding ->
        HomeContent(
            uiState = HomeUiState(),
            categories = previewHomeCategories(),
            onCategoryClick = {},
            onSeeAllClick = {},
            modifier = Modifier.padding(padding)
        )
    }
}
