package com.example.vetbook.presentation.screens.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.components.ServiceCard
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.Background

@Composable
fun ServiceScreen(
    categories: List<ServiceCategory>,
    onCategoryClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        SimpleTopBar(
            title = "Dịch vụ",
            onBackClick = onBackClick
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = categories,
                key = { it.id }
            ) { category ->
                ServiceCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.size(64.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = HealthPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ServiceScreenPreview() {
    val categories = listOf(
        ServiceCategory("cat_vet", "Veterinary Care", "Comprehensive pet care services", com.example.vetbook.R.drawable.services, "https://images.unsplash.com/photo-1628009368231-7bb7cfcb0def?q=80&w=2070"),
        ServiceCategory("cat_hotel", "Pet Hotel", "Comfortable boarding for your pet", com.example.vetbook.R.drawable.services, "https://images.unsplash.com/photo-1541599540903-21b1284cfe5a?q=80&w=2070"),
        ServiceCategory("cat_groom", "Pet Grooming", "Professional grooming services", com.example.vetbook.R.drawable.services, "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?q=80&w=2070"),
        ServiceCategory("cat_train", "Pet Training", "Training sessions for your pet", com.example.vetbook.R.drawable.services, "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?q=80&w=2069")
    )
    PreviewNavScaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ServiceScreen(
                categories = categories,
                onCategoryClick = {},
                onBackClick = {}
            )
        }
    }
}