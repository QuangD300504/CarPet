package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.vetbook.presentation.components.store.OrderSummaryCard
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.models.CartItem
import com.example.vetbook.presentation.models.OrderSummary
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Divider
import com.example.vetbook.presentation.viewmodels.CheckoutViewModel

@Composable
fun CartScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onOrderHistoryClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val cartItems = uiState.lines.map { line ->
        CartItem(
            id = line.product.id,
            shopName = line.product.shopName,
            productName = line.product.name,
            category = line.product.category ?: "",
            quantity = line.quantity,
            price = line.product.price.toString(),
            productId = line.product.id
        )
    }

    val orderSummary = OrderSummary(
        itemCount = uiState.itemCount,
        subtotal = uiState.subtotal,
        discount = uiState.discount,
        deliveryCharges = uiState.deliveryCharges
    )

    Scaffold(
        containerColor = Background
    ) { padding ->
        CartContent(
            uiState = uiState,
            cartItems = cartItems,
            orderSummary = orderSummary,
            onQuantityChange = { productId, qty -> viewModel.setQuantity(productId, qty) },
            onCheckoutClick = onCheckoutClick,
            onProductClick = onProductClick,
            onOrderHistoryClick = onOrderHistoryClick,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun CartContent(
    uiState: CheckoutViewModel.UiState,
    cartItems: List<CartItem>,
    orderSummary: OrderSummary,
    onQuantityChange: (String, Int) -> Unit,
    onCheckoutClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onOrderHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = HealthPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "Lỗi tải giỏ hàng",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = HealthMuted.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Giỏ hàng trống",
                    color = HealthMuted,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = cartItems,
                        key = { it.id }
                    ) { item ->
                        CartItemCard(
                            item = item,
                            imageUrl = uiState.lines.find { it.product.id == item.productId }?.product?.imageUrl,
                            quantity = item.quantity,
                            onQuantityChange = { newQty ->
                                onQuantityChange(item.productId, newQty)
                            },
                            onEditClick = { onProductClick(item.productId) }
                        )
                    }

                    item {
                        OrderSummaryCard(orderSummary = orderSummary)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = uiState.itemCount > 0
                        ) {
                            Text(
                                text = "Thanh Toán (${com.example.vetbook.utils.CurrencyFormatter.format(orderSummary.total)})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = onOrderHistoryClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Xem lịch sử đơn hàng",
                                color = HealthPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    imageUrl: String?,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = HealthPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.shopName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Chỉnh sửa",
                        fontSize = 13.sp,
                        color = HealthPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Image placeholder (replace with actual image if available)
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.productName,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = com.example.vetbook.R.drawable.ic_launcher_background),
                    error = painterResource(id = com.example.vetbook.R.drawable.ic_launcher_background)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.productName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = HealthMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = com.example.vetbook.utils.CurrencyFormatter.format(item.price),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HealthPrimary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(HealthSurface)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                                    contentDescription = if (quantity > 1) "Giảm" else "Xóa",
                                    tint = if (quantity > 1) HealthPrimary else Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = quantity.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { onQuantityChange(quantity + 1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tăng",
                                    tint = HealthPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
