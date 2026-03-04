package com.example.vetbook.presentation.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.admin.AdminOrdersViewModel

@Composable
fun AdminOrdersScreen(
    viewModel: AdminOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center), color = Brand
            )
            uiState.orders.isEmpty() -> Text(
                "No orders yet.", color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.orders) { order ->
                    AdminOrderCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(order: Map<String, Any?>) {
    val orderId = order["id"] as? String ?: ""
    val userId = order["userId"] as? String ?: ""
    val status = order["status"] as? String ?: "unknown"
    val total = order["total"]
    val createdAt = order["createdAt"] as? Long

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Order #${orderId.takeLast(8)}",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp
                )
                OrderStatusChip(status)
            }
            Text("User: ${userId.takeLast(12)}...", fontSize = 12.sp, color = Color.Gray)
            if (total != null) {
                Text(
                    "Total: ${total}đ",
                    fontSize = 13.sp, color = Brand, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OrderStatusChip(status: String) {
    val bg = when (status.lowercase()) {
        "completed", "delivered" -> Color(0xFFDCFCE7)
        "cancelled" -> Color(0xFFFEE2E2)
        "pending", "processing" -> Color(0xFFFEF9C3)
        else -> Color(0xFFF1F5F9)
    }
    val fg = when (status.lowercase()) {
        "completed", "delivered" -> Color(0xFF16A34A)
        "cancelled" -> Color(0xFFDC2626)
        "pending", "processing" -> Color(0xFFCA8A04)
        else -> Color.Gray
    }
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
