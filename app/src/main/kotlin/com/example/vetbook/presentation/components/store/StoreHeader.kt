package com.example.vetbook.presentation.components.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
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
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.vetbook.R
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Divider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreHeader(
    currentLocation: String = "TP. Hồ Chí Minh",
    onLocationClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    profileImageUrl: String? = null,
    showSearchBar: Boolean = true,
    searchPlaceholder: String = "Tìm kiếm sản phẩm...",
    onSearchChange: (String) -> Unit = {},
    searchValue: String = "",
    hasUnreadNotifications: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Location and icons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location selector
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocationClick() }
            ) {
                Text(
                    text = "Vị Trí",
                    fontSize = 12.sp,
                    color = HealthMuted,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = HealthPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentLocation,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = TextPrimary
                    )
                }
            }
            
            // Icons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cart icon
                Surface(
                    onClick = onCartClick,
                    shape = CircleShape,
                    color = HealthSurface,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Giỏ Hàng",
                            tint = HealthPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                // Profile avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(HealthSurface)
                        .border(
                            width = 1.dp,
                            color = Divider,
                            shape = CircleShape
                        )
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl != null) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Hồ Sơ",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Hồ Sơ",
                            tint = HealthPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
        
        if (showSearchBar) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Modern Search bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = searchValue,
                    onValueChange = onSearchChange,
                    placeholder = { 
                        Text(
                            text = searchPlaceholder,
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
                
                // Filter icon
                Surface(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    color = HealthPrimary,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Lọc",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationDropdown(
    cities: List<String>,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            cities.forEach { city ->
                Text(
                    text = city,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCitySelected(city) }
                        .padding(12.dp),
                    fontSize = 14.sp,
                    fontWeight = if (city == selectedCity) FontWeight.Bold else FontWeight.Normal,
                    color = if (city == selectedCity) HealthPrimary else TextPrimary
                )
            }
        }
    }
}
