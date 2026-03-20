package com.example.vetbook.presentation.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.OrderStatus
import com.example.vetbook.domain.models.StoreOrder
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.viewmodels.OrderHistoryTab
import com.example.vetbook.presentation.viewmodels.OrderHistoryUiState
import com.example.vetbook.presentation.viewmodels.OrderHistoryViewModel
import com.example.vetbook.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderHistoryScreen(
    viewModel: OrderHistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBackClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Lịch sử đơn hàng",
                onBackClick = onBackClick
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = Color.White,
                contentColor = HealthPrimary
            ) {
                OrderHistoryTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = HealthPrimary,
                        unselectedContentColor = HealthMuted
                    )
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HealthPrimary)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage ?: "Không thể tải đơn hàng",
                                color = Color.Red,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                uiState.filteredOrders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = HealthMuted.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Chưa có đơn hàng nào",
                                color = HealthMuted,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredOrders,
                            key = { it.id }
                        ) { order ->
                            OrderCard(
                                order = order,
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: StoreOrder,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")) }
    val dateStr = remember(order.createdAt) {
        dateFormatter.format(Date(order.createdAt))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.orderCode.takeLast(8)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                StatusBadge(status = order.status)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateStr,
                    fontSize = 13.sp,
                    color = HealthMuted
                )
                Text(
                    text = "${order.itemCount} sản phẩm",
                    fontSize = 13.sp,
                    color = HealthMuted
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.status.displayName,
                    fontSize = 13.sp,
                    color = HealthMuted
                )
                Text(
                    text = CurrencyFormatter.format(order.total),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthPrimary
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFF8E1) to Color(0xFFF59E0B)
        OrderStatus.PAID -> Color(0xFFE3F2FD) to Color(0xFF3B82F6)
        OrderStatus.SHIPPED -> Color(0xFFF3E5F5) to Color(0xFF8B5CF6)
        OrderStatus.DELIVERED -> Color(0xFFE8F5E9) to Color(0xFF22C55E)
        OrderStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFEF4444)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
