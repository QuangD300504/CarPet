package com.example.vetbook.presentation.components.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.models.OrderSummary

@Composable
fun OrderSummaryCard(
    orderSummary: OrderSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OrderSummaryRow("Items", "${orderSummary.itemCount}", Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OrderSummaryRow("Subtotal", "$${orderSummary.subtotal.toInt()}", Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OrderSummaryRow("Discount", "-$${orderSummary.discount.toInt()}", Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OrderSummaryRow("Delivery Charges", "$${orderSummary.deliveryCharges.toInt()}", Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            OrderSummaryRow("Total", "$${orderSummary.total.toInt()}", Color.Black, FontWeight.Bold)
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

