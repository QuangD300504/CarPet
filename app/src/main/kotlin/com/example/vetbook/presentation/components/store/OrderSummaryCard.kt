package com.example.vetbook.presentation.components.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.models.OrderSummary
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.Divider

@Composable
fun OrderSummaryCard(
    orderSummary: OrderSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Thông tin thanh toán",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OrderSummaryRow("Số lượng sản phẩm", "${orderSummary.itemCount}", HealthMuted)
            Spacer(modifier = Modifier.height(10.dp))
            OrderSummaryRow("Tạm tính", com.example.vetbook.utils.CurrencyFormatter.format(orderSummary.subtotal), HealthMuted)
            Spacer(modifier = Modifier.height(10.dp))
            OrderSummaryRow("Giảm giá", "-${com.example.vetbook.utils.CurrencyFormatter.format(orderSummary.discount)}", Color(0xFF22C55E)) // Success green
            Spacer(modifier = Modifier.height(10.dp))
            OrderSummaryRow("Phí vận chuyển", com.example.vetbook.utils.CurrencyFormatter.format(orderSummary.deliveryCharges), HealthMuted)
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Divider)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tổng cộng",
                    fontSize = 18.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = com.example.vetbook.utils.CurrencyFormatter.format(orderSummary.total),
                    fontSize = 18.sp,
                    color = HealthPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun OrderSummaryRow(
    label: String,
    value: String,
    color: Color,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = color,
            fontWeight = fontWeight
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = color,
            fontWeight = fontWeight
        )
    }
}
