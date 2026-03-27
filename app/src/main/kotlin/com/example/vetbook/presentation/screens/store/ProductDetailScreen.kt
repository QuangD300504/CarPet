package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.common.VetBookImage
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.viewmodels.ProductDetailViewModel
import com.example.vetbook.utils.CurrencyFormatter

@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.addedToCart) {
        if (uiState.addedToCart) {
            onNavigateToCart()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopBar(
                title = uiState.product?.name ?: "Chi tiết sản phẩm",
                onBackClick = onBackClick
            )
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = HealthPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Sản phẩm không tồn tại",
                            color = Color.Red
                        )
                    }
                }

                uiState.product != null -> {
                    ProductDetailContent(
                        product = uiState.product!!,
                        cartQuantity = uiState.cartQuantity,
                        onAddToCart = viewModel::addToCart
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: com.example.vetbook.domain.models.StoreProduct,
    cartQuantity: Int,
    onAddToCart: () -> Unit
) {
    val isOutOfStock = product.stock <= 0

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Product Image
            VetBookImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentScale = ContentScale.Fit,
                fallbackIcon = Icons.Default.Inventory,
                fallbackIconSize = 64.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Category chip + Stock badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    product.category?.let { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HealthSurface
                        ) {
                            Text(
                                text = cat.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HealthPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOutOfStock) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = if (isOutOfStock) "Hết hàng" else "Còn hàng",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOutOfStock) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Product name
                Text(
                    text = product.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    lineHeight = 32.sp
                )

                Spacer(Modifier.height(8.dp))

                // Shop name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.shopName,
                        fontSize = 14.sp,
                        color = HealthMuted
                    )
                    if (cartQuantity > 0) {
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HealthPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Đã có $cartQuantity trong giỏ",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 12.sp,
                                color = HealthPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Price
                Text(
                    text = CurrencyFormatter.format(product.price),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthPrimary
                )

                if (!isOutOfStock) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${product.stock} sản phẩm có sẵn",
                        fontSize = 13.sp,
                        color = HealthMuted
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Description
                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = "Mô tả",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = product.description,
                        fontSize = 15.sp,
                        color = HealthMuted,
                        lineHeight = 26.sp
                    )
                }
            }
        }

        // Sticky bottom bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = CurrencyFormatter.format(product.price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthPrimary
                    )
                    if (isOutOfStock) {
                        Text(
                            text = "Hết hàng",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }
                }

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.height(52.dp),
                    enabled = !isOutOfStock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HealthPrimary,
                        disabledContainerColor = HealthMuted.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Thêm vào giỏ hàng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}