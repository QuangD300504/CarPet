package com.example.vetbook.presentation.screens.accommodation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.Coordinates
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.BrandSurface
import com.example.vetbook.presentation.theme.Success

@Composable
fun AccommodationDetailScreen(
    accommodation: Accommodation,
    onBackClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Type-C style hero header ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Brand, Brand.copy(alpha = 0.7f))
                        )
                    )
            ) {
                // Category icon centred
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (accommodation.category) {
                            AccommodationCategory.HOTEL    -> Icons.Default.Hotel
                            AccommodationCategory.HOMESTAY -> Icons.Default.Home
                            AccommodationCategory.APART    -> Icons.Default.Apartment
                            AccommodationCategory.COFFEE   -> Icons.Default.LocalCafe
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = accommodation.category.displayName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                            )
                        )
                )
            }

            // ── Content card ─────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = accommodation.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (accommodation.isPopular) {
                            Surface(
                                color = Success.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Popular",
                                    color = Success,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${accommodation.location}, ${accommodation.district}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating + price row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stars
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val ratingInt = accommodation.rating.toInt()
                            repeat(5) { i ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < ratingInt) Brand else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${accommodation.rating} (${accommodation.reviewCount})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Price
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${accommodation.price.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Brand
                            )
                            Text(
                                text = "/${accommodation.priceUnit}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Description
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = accommodation.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Amenities
                    Text(
                        text = "Amenities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Pet-friendly", "Garden", "Free WiFi", "24/7 Care").forEach { tag ->
                            Surface(
                                color = BrandSurface,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    color = Brand,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Book Now button
                    Button(
                        onClick = { /* TODO: booking flow */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Book Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Floating back button (Type C)
        Box(
            modifier = Modifier
                .padding(top = 44.dp, start = 16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
