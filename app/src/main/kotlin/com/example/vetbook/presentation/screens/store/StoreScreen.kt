package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.store.LocationDropdown
import com.example.vetbook.presentation.components.store.ProductCard
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.BrandSurface
import com.example.vetbook.presentation.viewmodels.StoreViewModel
import com.example.vetbook.presentation.viewmodels.StoreUiState

@Composable
fun StoreScreen(
    viewModel: StoreViewModel = hiltViewModel(),
    showLocationDropdown: Boolean = false,
    onLocationDropdownDismiss: () -> Unit = {},
    onProductsClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentLocation by remember { mutableStateOf("Ho Chi Minh City") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            StoreContent(
                uiState = uiState,
                showLocationDropdown = showLocationDropdown,
                currentLocation = currentLocation,
                onLocationDropdownDismiss = onLocationDropdownDismiss,
                onCitySelected = { currentLocation = it },
                onProductsClick = onProductsClick,
                onCategoryClick = { label ->
                    val category = label.lowercase()
                    viewModel.setCategory(category)
                    onCategoryClick(category)
                },
                onAddToCart = viewModel::addToCart
            )
        }
    }
}

@Composable
private fun StoreContent(
    uiState: StoreUiState,
    showLocationDropdown: Boolean,
    currentLocation: String,
    onLocationDropdownDismiss: () -> Unit,
    onCitySelected: (String) -> Unit,
    onProductsClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onAddToCart: (String) -> Unit
) {
    val cities = remember {
        listOf(
            "Ho Chi Minh City", "Binh Duong", "Dong Nai", "Ba Ria-Vung Tau",
            "Long An", "Tien Giang", "Ben Tre", "Tra Vinh", "Vinh Long",
            "Dong Thap", "An Giang", "Kien Giang", "Can Tho", "Hau Giang"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showLocationDropdown) {
            LocationDropdown(
                cities = cities,
                selectedCity = currentLocation,
                onCitySelected = {
                    onCitySelected(it)
                    onLocationDropdownDismiss()
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }

            item {
                Text(
                    text = "Explore Categories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = listOf(
                            "Foods" to Icons.Default.Restaurant,
                            "Toys" to Icons.Default.SportsEsports,
                            "Accessories" to Icons.Default.ShoppingBag,
                            "Hygiene" to Icons.Default.Spa,
                            "Kennel" to Icons.Default.Home
                        ),
                        key = { it.first }
                    ) { (label, icon) ->
                        CategoryIcon(
                            label = label,
                            icon = icon,
                            onClick = { onCategoryClick(label) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggested For You",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text     = "View all >",
                        style    = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color    = Brand,
                        modifier = Modifier.clickable { onProductsClick() }
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brand)
                    }
                }
            } else if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage ?: "Failed to load products",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.products,
                            key = { it.id }
                        ) { product ->
                            Box(modifier = Modifier.width(160.dp)) {
                                ProductCard(
                                    product = product,
                                    onClick = { onProductsClick() },
                                    onAddToCart = { onAddToCart(product.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryIcon(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier         = Modifier
                .size(64.dp)
                .background(BrandSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Brand,
                modifier           = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun StoreScreenPreview() {
    PreviewNavScaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            StoreScreen()
        }
    }
}
