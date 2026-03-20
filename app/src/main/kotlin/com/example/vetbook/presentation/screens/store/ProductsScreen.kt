package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.store.ProductCard
import com.example.vetbook.presentation.components.store.StoreHeader
import com.example.vetbook.presentation.components.store.FilterBottomSheet
import com.example.vetbook.presentation.viewmodels.SortOption
import com.example.vetbook.presentation.viewmodels.StoreViewModel
import com.example.vetbook.presentation.viewmodels.StoreUiState
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    category: String? = null,
    viewModel: StoreViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onFilterClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(category) {
        searchQuery = ""
        viewModel.setSearchQuery("")
        viewModel.setCategory(category)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StoreHeader(
                currentLocation = "Ho Chi Minh City",
                onLocationClick = { },
                onCartClick = onCartClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                showSearchBar = true,
                searchValue = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                profileImageUrl = null,
                onBackClick = { onBackClick() }
            )

            ProductsContent(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                onCategoryChange = { newCategory ->
                    viewModel.setCategory(newCategory)
                },
                onAddToCart = viewModel::addToCart,
                onProductClick = onProductClick,
                onFilterClick = onFilterClick ?: { viewModel.setFilterSheetVisible(true) },
                showSearchBar = false
            )
        }

        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                currentSort = uiState.sortOption,
                currentPriceMin = uiState.priceRangeMin,
                currentPriceMax = uiState.priceRangeMax,
                currentInStockOnly = uiState.inStockOnly,
                onSortChange = viewModel::setSortOption,
                onPriceRangeChange = { min, max -> viewModel.setPriceRange(min, max) },
                onInStockChange = { viewModel.setInStockOnly(it) },
                onReset = {
                    viewModel.setSortOption(SortOption.NEWEST)
                    viewModel.setPriceRange(0f, 5_000_000f)
                    viewModel.setInStockOnly(false)
                },
                onApply = { viewModel.setFilterSheetVisible(false) },
                sheetState = sheetState
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) HealthPrimary else HealthSurface,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else HealthPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) HealthPrimary else HealthSurface,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else HealthPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsContent(
    uiState: StoreUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onAddToCart: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    showSearchBar: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (showSearchBar) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Tìm kiếm sản phẩm...",
                            color = HealthMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = HealthMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                        unfocusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                        disabledContainerColor = HealthSurface.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = HealthPrimary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Surface(
                    onClick = onFilterClick,
                    shape = RoundedCornerShape(12.dp),
                    color = HealthPrimary,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Bộ lọc",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = "Sản Phẩm",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CategoryChip(
                    label = "Tất cả",
                    icon = Icons.Default.GridView,
                    selected = uiState.selectedCategory == null,
                    onClick = { onCategoryChange(null) }
                )
            }
            val categories = listOf(
                Triple("Thức ăn", "foods", Icons.Default.Restaurant),
                Triple("Đồ chơi", "toys", Icons.Default.SportsEsports),
                Triple("Phụ kiện", "accessories", Icons.Default.ShoppingBag),
                Triple("Vệ sinh", "hygiene", Icons.Default.Spa),
                Triple("Chuồng nuôi", "habitat", Icons.Default.Home)
            )
            items(categories) { (label, key, icon) ->
                CategoryChip(
                    label = label,
                    icon = icon,
                    selected = uiState.selectedCategory == key,
                    onClick = { onCategoryChange(key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product grid
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HealthPrimary)
            }
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "Lỗi tải sản phẩm",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else if (uiState.products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy sản phẩm",
                    color = HealthMuted,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { onAddToCart(product.id) }
                    )
                }
            }
        }
    }
}
