package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.vetbook.presentation.viewmodels.StoreViewModel
import com.example.vetbook.presentation.viewmodels.StoreUiState
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background

@Composable
fun ProductsScreen(
    category: String? = null,
    viewModel: StoreViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(category) {
        // When opening this screen from navigation, initialize filters once
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

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
                onAddToCart = viewModel::addToCart
            )
        }
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
private fun ProductsContent(
    uiState: StoreUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onAddToCart: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
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
                .fillMaxWidth()
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

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Sản Phẩm",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category chips (including "All") so users can filter directly on this screen
        val categories = listOf(
            Triple("Tất cả", null, Icons.Default.GridView),
            Triple("Thức ăn", "foods", Icons.Default.Restaurant),
            Triple("Đồ chơi", "toys", Icons.Default.SportsEsports),
            Triple("Phụ kiện", "accessories", Icons.Default.ShoppingBag),
            Triple("Vệ sinh", "hygiene", Icons.Default.Spa),
            Triple("Chuồng nuôi", "habitat", Icons.Default.Home)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { (label, key, icon) ->
                val isSelected = uiState.selectedCategory == key
                FilterChip(
                    label = label,
                    icon = icon,
                    selected = isSelected,
                    onClick = { onCategoryChange(key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HealthPrimary)
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage ?: "Lỗi tải sản phẩm", color = Color.Red)
            }
        } else if (uiState.products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = HealthMuted.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Không tìm thấy sản phẩm", color = HealthMuted)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(
                    items = uiState.products,
                    key = { it.id }
                ) { product ->
                    ProductCard(
                        product = product,
                        showFavorite = true,
                        onAddToCart = { onAddToCart(product.id) }
                    )
                }
            }
        }
    }
}
