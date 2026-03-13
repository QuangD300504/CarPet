package com.example.vetbook.presentation.screens.store

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.vetbook.utils.DeepLinkHandler
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.vetbook.presentation.components.store.OrderSummaryCard
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.models.OrderSummary
import com.example.vetbook.presentation.models.PaymentMethod
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Divider
import com.example.vetbook.presentation.viewmodels.CheckoutViewModel
import com.example.vetbook.utils.PayosLauncher

@Composable
fun PaymentScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCheckoutFinished: (Boolean) -> Unit = {},
    onShowPayment: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            onCheckoutFinished(true)
        }
    }

    LaunchedEffect(Unit) {
        DeepLinkHandler.paymentResult.collect { isSuccess ->
            if (isSuccess) {
                viewModel.onCheckoutSuccess()
            } else {
                // For store orders, we just stay on the screen or show a message
                // The order hasn't been created in Firestore yet in this flow
            }
            DeepLinkHandler.clear()
        }
    }

    var selectedPaymentMethod by remember { mutableStateOf("payos") }

    val paymentMethods = remember {
        listOf(
            PaymentMethod("payos", "Thanh toán PayOS", Icons.Default.QrCode),
            PaymentMethod("credit_card", "Thẻ Tín dụng / Ghi nợ", Icons.Default.CreditCard),
            PaymentMethod("cash", "Tiền mặt", Icons.Default.Payments)
        )
    }

    val orderSummary = OrderSummary(
        itemCount = uiState.itemCount,
        subtotal = uiState.subtotal,
        discount = uiState.discount,
        deliveryCharges = uiState.deliveryCharges
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val openPayOS: () -> Unit = {
        scope.launch {
            try {
                val url = viewModel.fetchPayosUrl()
                if (url.isBlank()) {
                    snackbarHostState.showSnackbar("Không thể tạo liên kết thanh toán. Vui lòng thử lại.")
                    return@launch
                }
                onShowPayment(url)
            } catch (e: Exception) {
                Log.e("PayOS", "fetchPayosUrl failed: ${e.message}")
                snackbarHostState.showSnackbar("Lỗi thanh toán: ${e.message ?: "Lỗi không xác định"}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = HealthPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Lỗi tải thông tin thanh toán",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                OrderSummaryCard(orderSummary = orderSummary)
                            }

                            item {
                                Text(
                                    text = "Chọn phương thức thanh toán",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(
                                items = paymentMethods,
                                key = { it.id }
                            ) { method ->
                                PaymentMethodCard(
                                    method = method,
                                    isSelected = selectedPaymentMethod == method.id,
                                    onClick = { selectedPaymentMethod = method.id }
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            Box(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
                                Button(
                                    onClick = {
                                        if (selectedPaymentMethod == "payos") {
                                            openPayOS()
                                        } else {
                                            viewModel.onCheckoutSuccess()
                                        }
                                    },
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
                                        text = "Xác nhận Thanh toán",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) HealthPrimary else Divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) HealthPrimary.copy(alpha = 0.1f) else HealthSurface,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = method.icon,
                            contentDescription = method.name,
                            tint = if (isSelected) HealthPrimary else HealthMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = method.name,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) HealthPrimary else TextPrimary
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by Card click
                colors = RadioButtonDefaults.colors(
                    selectedColor = HealthPrimary,
                    unselectedColor = HealthMuted
                )
            )
        }
    }
}
