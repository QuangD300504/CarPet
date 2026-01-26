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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.components.store.LocationDropdown
import com.example.vetbook.presentation.components.store.ProductCard
import com.example.vetbook.presentation.components.store.StoreHeader
import com.example.vetbook.presentation.models.Product

@Composable
fun StoreScreen(
    onProductsClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showLocationDropdown by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf("Ho Chi Minh City") }
    var searchValue by remember { mutableStateOf("") }
    
    val cities = remember {
        listOf(
            "Ho Chi Minh City", "Binh Duong", "Dong Nai", "Ba Ria-Vung Tau",
            "Long An", "Tien Giang", "Ben Tre", "Tra Vinh", "Vinh Long",
            "Dong Thap", "An Giang", "Kien Giang", "Can Tho", "Hau Giang"
        )
    }
    
    val sampleProducts = remember {
        (1..6).map { Product("$it", "Product $it", "${it * 10}") }
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
                // Category filters from Figma: Foods, Toys, Accessories, Hygiene, Kennel
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(26.dp)
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
                            onClick = { onCategoryClick(label.lowercase()) }
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
                        text = "View all >",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.clickable { onProductsClick() }
                    )
                }
            }
            
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(400.dp)
                ) {
                    items(
                        items = sampleProducts,
                        key = { it.id }
                    ) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductsClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryIcon(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(123.dp)
                .background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFFFD813), // Yellow from Figma
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StoreScreenPreview() {
    StoreScreen()
}
