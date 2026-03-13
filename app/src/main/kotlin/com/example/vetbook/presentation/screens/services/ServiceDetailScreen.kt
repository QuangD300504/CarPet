package com.example.vetbook.presentation.screens.services

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items // Quan trọng cho danh sách
import androidx.compose.foundation.shape.CircleShape
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
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.Background

@Composable
fun ServicePriceItem(servicePackage: ServicePackage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthSurface)
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
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            val formattedPriceString = com.example.vetbook.utils.CurrencyFormatter.format(servicePackage.price)
            Text(
                text = formattedPriceString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = HealthPrimary
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
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Gradient
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = detail.bannerGradientColors.map { Color(it) }
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 44.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                        }
                        IconButton(
                            onClick = { /* Like */ },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Yêu thích", tint = TextPrimary)
                        }
                    }
                }
            }

            // Information
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    val vnCategoryTitle = when(category.id) {
                        "cat_vet" -> "Thú y"
                        "cat_hotel" -> "Khách sạn"
                        "cat_groom" -> "Làm đẹp"
                        "cat_train" -> "Huấn luyện"
                        else -> category.title
                    }

                    Text(
                        text = vnCategoryTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                        Text(
                            text = " ${detail.rating} ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "(${detail.reviewCount} đánh giá)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Về chúng tôi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = detail.about,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Dịch vụ & Bảng giá",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }

            // Pricing Items
            items(
                items = detail.packages,
                key = { it.name }
            ) { pkg ->
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ServicePriceItem(servicePackage = pkg)
                }
            }

            // Available Today
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Lịch trống hôm nay",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(
                            items = detail.availableTimes,
                            key = { it }
                        ) { time ->
                            Button(
                                onClick = { /* Chọn giờ */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HealthSurface,
                                    contentColor = HealthPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(time, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
        
        // Book Now button
        Button(
            onClick = { /* Xử lý đặt lịch */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary)
        ) {
            Text(
                "Đặt ngay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
