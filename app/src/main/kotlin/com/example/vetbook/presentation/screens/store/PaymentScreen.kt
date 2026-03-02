package com.example.vetbook.presentation.screens.store

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.vnpay.authentication.VNP_AuthenticationActivity
import com.vnpay.authentication.VNP_SdkCompletedCallback
import com.example.vetbook.presentation.components.store.OrderSummaryCard
import com.example.vetbook.presentation.models.OrderSummary
import com.example.vetbook.presentation.models.PaymentMethod
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.viewmodels.CheckoutViewModel

@Composable
fun PaymentScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCheckoutFinished: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            onCheckoutFinished(true)
        }
    }

    var selectedPaymentMethod by remember { mutableStateOf("credit_card") }

    val paymentMethods = remember {
        listOf(
            PaymentMethod("paypal", "Paypal", Icons.Default.AccountBalance),
            PaymentMethod("credit_card", "Credit Card", Icons.Default.CreditCard),
            PaymentMethod("cash", "Cash", Icons.Default.Money)
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

    val openVNPAY: () -> Unit = {
        scope.launch {
            try {
                val url = viewModel.fetchVnpayUrl()
                if (url.isNullOrBlank()) {
                    snackbarHostState.showSnackbar("Could not generate payment URL. Please try again.")
                    return@launch
                }
                val intent = Intent(context, VNP_AuthenticationActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("tmn_code", "W8JDF86Z")
                intent.putExtra("scheme", "vetbook-vnpay")
                intent.putExtra("is_sandbox", true)

                VNP_AuthenticationActivity.setSdkCompletedCallback(object : VNP_SdkCompletedCallback {
                    override fun sdkAction(action: String) {
                        Log.d("VNPAY", "Action: $action")
                        val isSuccess = action == "SuccessBackAction"
                        if (isSuccess) {
                            viewModel.onCheckoutSuccess()
                            // Clear cart logic happens inside the ViewModel, which will flip checkoutSuccess to true when done.
                        } else {
                            onCheckoutFinished(false)
                        }
                    }
                })
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("VNPAY", "fetchVnpayUrl failed: ${e.message}")
                snackbarHostState.showSnackbar("Payment error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFD813))
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Failed to load checkout",
                    color = Color.Red
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OrderSummaryCard(orderSummary = orderSummary)
                }

                item {
                    Text(
                        text = "Choose payment method",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
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

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Button(
                        onClick = openVNPAY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEB3B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.itemCount > 0
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
    } // end Scaffold
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFEB3B))
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = method.name,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = method.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFFFFEB3B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    PreviewNavScaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            PaymentScreen()
        }
    }
}
