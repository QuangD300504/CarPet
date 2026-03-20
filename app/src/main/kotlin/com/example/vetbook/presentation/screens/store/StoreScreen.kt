package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.store.ProductCard
import com.example.vetbook.presentation.components.store.StoreHeader
import com.example.vetbook.presentation.components.topbars.HomeTopBar
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Divider
import com.example.vetbook.presentation.viewmodels.StoreViewModel
import com.example.vetbook.presentation.viewmodels.StoreUiState

@Composable
fun StoreScreen(
    viewModel: StoreViewModel = hiltViewModel(),
    onProductsClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    showHeader: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentLocation by remember { mutableStateOf("TP. Hồ Chí Minh") }
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

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
            if (showHeader) {
                StoreHeader(
                    currentLocation = currentLocation,
                    onLocationClick = { /* TODO: location picker */ },
                    onCartClick = onCartClick,
                    onNotificationClick = onNotificationClick,
                    onProfileClick = onProfileClick,
                    showSearchBar = true,
                    searchValue = uiState.searchQuery,
                    onSearchChange = viewModel::setSearchQuery,
                    profileImageUrl = null
                )
            }
            StoreContent(
                uiState = uiState,
                onProductsClick = onProductsClick,
                onCategoryClick = { category ->
                    viewModel.setCategory(category)
                    onCategoryClick(category)
                },
                onAddToCart = viewModel::addToCart,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StoreContent(
    uiState: StoreUiState,
    onProductsClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Promotional Banner
        item(span = { GridItemSpan(2) }) {
            PromotionalBanner()
        }

        // Categories Section Header
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Danh Mục",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        // Horizontal Categories Row
        item(span = { GridItemSpan(2) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
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
                            onClick = { onCategoryClick(key) }
                        )
                    }
                }
            }
        }

        // Suggested Products Header
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gợi Ý Cho Bạn",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Xem tất cả",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthPrimary,
                    modifier = Modifier.clickable { onProductsClick() }
                )
            }
        }

        // Product Grid
        if (uiState.isLoading) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HealthPrimary)
                }
            }
        } else if (uiState.errorMessage != null) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = uiState.errorMessage ?: "Lỗi tải sản phẩm",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(uiState.products) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductsClick() },
                    onAddToCart = { onAddToCart(product.id) }
                )
            }
        }
    }
}

@Composable
private fun PromotionalBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = HealthPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "HÀNG MỚI VỀ",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Thực phẩm sạch\nCho thú cưng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthPrimary,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mua ngay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = HealthPrimary,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = HealthPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
