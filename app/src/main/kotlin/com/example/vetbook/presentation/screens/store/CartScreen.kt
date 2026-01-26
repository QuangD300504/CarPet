package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.R
import com.example.vetbook.presentation.components.store.OrderSummaryCard
import com.example.vetbook.presentation.models.CartItem
import com.example.vetbook.presentation.models.OrderSummary

@Composable
fun CartScreen(
    onBackClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {}
) {
    var quantities by remember {
        mutableStateOf(mapOf("1" to 1, "2" to 1, "3" to 1))
    }
    
    val cartItems = remember {
        listOf(
            CartItem("1", "Petvet Shop", "Pate Cột Đèn Hải Phòng cho cún", "Phân loại", 1, "36", "1"),
            CartItem("2", "Petvet Shop", "Pate Cột Đèn Hải Phòng cho cún", "Phân loại", 1, "36", "2"),
            CartItem("3", "Petvet Shop", "Pate Cột Đèn Hải Phòng cho cún", "Phân loại", 1, "36", "3")
        )
    }
    
    val orderSummary = remember {
        OrderSummary(
            itemCount = cartItems.size,
            subtotal = 423.0,
            discount = 4.0,
            deliveryCharges = 2.0
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Yellow top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEB3B))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = Color.Black
                    )
                }
                IconButton(onClick = { }) {
                    BadgedBox(
                        badge = { Badge() }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Black
                        )
                    }
                }
                Image(
                    painter = painterResource(R.drawable.pawns),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Giỏ hàng",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(
                items = cartItems,
                key = { it.id }
            ) { item ->
                CartItemCard(
                    item = item,
                    quantity = quantities[item.id] ?: 1,
                    onQuantityChange = { newQty ->
                        quantities = quantities + (item.id to newQty)
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                OrderSummaryCard(orderSummary = orderSummary)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Button(
                    onClick = onCheckoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Check Out",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.shopName} >",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                TextButton(onClick = { }) {
                    Text(
                        text = "Edit",
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.productName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = quantity.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        IconButton(
                            onClick = { onQuantityChange(quantity + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = "${item.price}$",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    CartScreen()
}
