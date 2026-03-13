package com.example.vetbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Banner
import com.example.vetbook.presentation.theme.TextPrimary

@Composable
fun SponsoredSection(
    banners: List<Banner> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "Featured Offers",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (banners.isEmpty()) {
                items(3) { SponsoredCard() }
            } else {
                items(banners) { banner ->
                    BannerCard(banner = banner)
                }
            }
        }
    }
}

@Composable
fun BannerCard(banner: Banner, modifier: Modifier = Modifier) {
    AsyncImage(
        model = banner.imageUrl.ifBlank { "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?q=80&w=1964&auto=format&fit=crop" },
        contentDescription = banner.title,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .width(160.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF2F2F2))
    )
}

@Composable
fun SponsoredCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF2F2F2))
    )
}
