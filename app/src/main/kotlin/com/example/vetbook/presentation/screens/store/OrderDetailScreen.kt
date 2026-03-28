package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
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
import com.example.vetbook.domain.models.OrderItem
import com.example.vetbook.domain.models.OrderStatus
import com.example.vetbook.domain.models.StoreOrder
import com.example.vetbook.presentation.components.common.VetBookImage
import com.example.vetbook.presentation.components.store.OrderSummaryCard
import com.example.vetbook.presentation.models.OrderSummary
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.viewmodels.OrderHistoryViewModel
import com.example.vetbook.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrderHistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onRetryPayment: (checkoutUrl: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var order by remember { mutableStateOf<StoreOrder?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(orderId) {
        // First check if already in cached orders
        order = uiState.allOrders.find { it.id == orderId }
        if (order == null) {
            // Fetch directly
            order = viewModel.getOrderById(orderId)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chi tiết đơn #${order?.orderCode?.takeLast(8) ?: orderId.take(8)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = HealthPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                order == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Không tìm thấy đơn hàng",
                            color = HealthMuted
                        )
                    }
                }

                else -> {
                    OrderDetailContent(order = order!!, onRetryPayment = onRetryPayment)
                }
            }
        }
    }
}

@Composable
private fun OrderDetailContent(order: StoreOrder, onRetryPayment: (String) -> Unit = {}) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")) }
    val dateStr = remember(order.createdAt) {
        dateFormatter.format(Date(order.createdAt))
    }

    val orderSummary = remember(order) {
        OrderSummary(
            itemCount = order.itemCount,
            subtotal = order.subtotal,
            discount = order.discount,
            deliveryCharges = order.deliveryCharges
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${order.orderCode.takeLast(8)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        StatusBadge(status = order.status)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Ngày đặt: $dateStr",
                        fontSize = 14.sp,
                        color = HealthMuted
                    )
                }
            }
        }

        // STO-02: Retry payment button for PENDING orders
        if (order.status == OrderStatus.PENDING && !order.checkoutUrl.isNullOrBlank()) {
            item {
                Button(
                    onClick = { onRetryPayment(order.checkoutUrl!!) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Tiếp tục thanh toán", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Delivery address card
        if (!order.receiverName.isNullOrBlank() || !order.deliveryAddress.isNullOrBlank()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Địa chỉ giao hàng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        order.receiverName?.let { Text(it, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) }
                        order.receiverPhone?.let { Text(it, fontSize = 13.sp, color = HealthMuted) }
                        order.deliveryAddress?.let { Text(it, fontSize = 13.sp, color = HealthMuted) }
                    }
                }
            }
        }

        // Items section
        item {
            Text(
                text = "Sản phẩm (${order.itemCount})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(order.items) { item ->
            OrderItemCard(item = item)
        }

        // Order summary
        item {
            OrderSummaryCard(orderSummary = orderSummary)
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VetBookImage(
                model = item.imageUrl,
                contentDescription = item.productName,
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(10.dp),
                contentScale = ContentScale.Fit,
                fallbackIcon = androidx.compose.material.icons.Icons.Default.Inventory,
                fallbackIconSize = 28.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "SL: ${item.quantity}",
                    fontSize = 13.sp,
                    color = HealthMuted
                )
            }

            Text(
                text = CurrencyFormatter.format(item.lineTotal),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HealthPrimary
            )
        }
    }
}