package com.example.vetbook.presentation.components.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.models.Product

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showFavorite: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showFavorite) 150.dp else 120.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = if (showFavorite) Alignment.TopEnd else Alignment.Center
            ) {
                if (showFavorite) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = if (showFavorite) 12.dp else 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (showFavorite) 12.dp else 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priceText = remember(product.price) {
                    "$${product.price}"
                }
                Text(
                    text = priceText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(if (showFavorite) 36.dp else 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to cart",
                        tint = if (showFavorite) Color.White else Color(0xFFFFEB3B),
                        modifier = Modifier
                            .size(if (showFavorite) 28.dp else 24.dp)
                            .background(
                                Color(if (showFavorite) 0xFFFFEB3B else 0xFFFFEB3B),
                                CircleShape
                            )
                            .padding(if (showFavorite) 6.dp else 4.dp)
                    )
                }
            }
        }
    }
}

