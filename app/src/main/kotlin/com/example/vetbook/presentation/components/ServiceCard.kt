package com.example.vetbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.HealthMuted

@Composable
fun ServiceCard(
    category: ServiceCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val defaultImageUrl = when (category.id) {
        "cat_vet", "cat_training" -> "https://images.unsplash.com/photo-1576201836106-db1758fd1c97?q=80&w=2070&auto=format&fit=crop"
        "cat_hotel", "cat_party" -> "https://images.unsplash.com/photo-1591768793355-74d0acaec663?q=80&w=2070&auto=format&fit=crop"
        "cat_spa", "cat_funeral" -> "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?q=80&w=2071&auto=format&fit=crop"
        "cat_shop", "cat_walk" -> "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?q=80&w=2070&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?q=80&w=2069&auto=format&fit=crop"
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .height(240.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
            ) {
                AsyncImage(
                    model = category.imageUrl ?: defaultImageUrl,
                    contentDescription = category.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "PHỔ BIẾN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = category.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.shortDescription,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HealthMuted,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
