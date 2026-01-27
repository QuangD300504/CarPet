package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.components.store.LocationDropdown
import com.example.vetbook.presentation.components.store.ProductCard
import com.example.vetbook.presentation.components.store.StoreHeader
import com.example.vetbook.presentation.models.Product
import com.example.vetbook.presentation.previews.PreviewNavScaffold

@Composable
fun ProductsScreen(
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showLocationDropdown by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf("Ho Chi Minh City") }
    var searchValue by remember { mutableStateOf("") }
    
    val products = remember {
        listOf(
            Product("1", "Pate", "40"),
            Product("2", "Pate", "430"),
            Product("3", "Pate", "330"),
            Product("4", "Pate", "333"),
            Product("5", "Pate", "50"),
            Product("6", "Pate", "400")
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        StoreHeader(
            currentLocation = currentLocation,
            onLocationClick = { showLocationDropdown = !showLocationDropdown },
            onCartClick = onCartClick,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            searchValue = searchValue,
            onSearchChange = { searchValue = it }
        )
        
        if (showLocationDropdown) {
            val cities = remember {
                listOf(
                    "Ho Chi Minh City", "Binh Duong", "Dong Nai", "Ba Ria-Vung Tau",
                    "Long An", "Tien Giang", "Ben Tre", "Tra Vinh", "Vinh Long",
                    "Dong Thap", "An Giang", "Kien Giang", "Can Tho", "Hau Giang"
                )
            }
            LocationDropdown(
                cities = cities,
                selectedCity = currentLocation,
                onCitySelected = { 
                    currentLocation = it
                    showLocationDropdown = false
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Products",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = products,
                    key = { it.id }
                ) { product ->
                    ProductCard(
                        product = product,
                        showFavorite = true,
                        onAddToCart = { }
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ProductsScreenPreview() {
    PreviewNavScaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ProductsScreen()
        }
    }
}
