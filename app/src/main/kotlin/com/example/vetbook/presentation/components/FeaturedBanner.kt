package com.example.vetbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BannerEvent(
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val gradientColors: List<Color>
)

val sampleEvents = listOf(
    BannerEvent(
        title = "Tìm Y viện",
        subtitle = "24/7",
        buttonText = "Tìm",
        gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
    ),
    BannerEvent(
        title = "Ưu đãi Grooming",
        subtitle = "Giảm 20% cho lần đầu",
        buttonText = "Đặt lịch",
        gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
    ),
    BannerEvent(
        title = "Pet Hotel",
        subtitle = "Chăm sóc tận tâm",
        buttonText = "Khám phá",
        gradientColors = listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2))
    )
)

@Composable
fun FeaturedBanner() {
    val pagerState = rememberPagerState(pageCount = { sampleEvents.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val event = sampleEvents[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.horizontalGradient(colors = event.gradientColors),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = event.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.subtitle,
                        color = Color.White.copy(alpha = 0.9f), // Increased opacity
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp), // Adjust padding
                        modifier = Modifier.width(110.dp).height(40.dp) // Fixed width for full text
                    ) {
                        Text(
                            text = event.buttonText,
                            color = event.gradientColors.last(),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pager Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(sampleEvents.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color(0xFFFF9800) else Color.LightGray
                val width = if (pagerState.currentPage == iteration) 18.dp else 8.dp
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(width = width, height = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun FeaturedBannerPreview() {
    FeaturedBanner()
}
