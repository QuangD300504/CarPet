package com.example.vetbook.presentation.screens.service_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items // Quan trọng cho danh sách
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.PetServiceDetail
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.models.ServicePackage

@Composable
fun ServicePriceItem(servicePackage: ServicePackage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = servicePackage.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Text(
                text = "$${servicePackage.price.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF9A825) // Màu cam vàng cho giá tiền
            )
        }
    }
}

@Composable
fun ServiceDetailScreen(
    category: ServiceCategory,
    detail: PetServiceDetail,
    onBackClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            // Nút Book Now cố định ở dưới
            Button(
                onClick = { /* Xử lý đặt lịch */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
            ) {
                Text("Book Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Header Gradient với nút Back/Heart
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = detail.bannerGradientColors.map { Color(it) }
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Gray)
                        }
                        IconButton(onClick = { /* Like */ }) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }

            // 2. Nội dung thông tin chi tiết
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB74D))
                        Text(text = " ${detail.rating} ", fontWeight = FontWeight.Bold)
                        Text(text = "(${detail.reviewCount})", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "About", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(text = detail.about, color = Color.Gray, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Services & Pricing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }

            // 3. Danh sách giá dịch vụ con
            items(detail.packages) { pkg ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ServicePriceItem(servicePackage = pkg)
                }
            }

            // 4. Khung giờ Available Today
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Available Today", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(detail.availableTimes) { time ->
                            OutlinedButton(
                                onClick = { /* Chọn giờ */ },
                                border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(time, color = Color(0xFFFFB74D))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp)) // Padding cuối để không bị nút Book Now che
                }
            }
        }
    }
}