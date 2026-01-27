package com.example.vetbook.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.presentation.previews.PreviewNavScaffold

@Composable
fun ServiceScreen(
    categories: List<ServiceCategory>,
    onCategoryClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = categories,
                key = { it.id }
            ) { category ->
                CategoryGridCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryGridCard(
    category: ServiceCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp), // Fixed height from Figma
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F2F2) // Light grey from Figma
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(category.iconRes),
                    contentDescription = category.title,
                    modifier = Modifier.size(35.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFFFD813)) // Yellow from Figma
                )
            }
            Text(
                text = category.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ServiceScreenPreview() {
    val categories = listOf(
        ServiceCategory("cat_vet", "Veterinary Care", "Comprehensive pet care services", com.example.vetbook.R.drawable.services),
        ServiceCategory("cat_hotel", "Pet Hotel", "Comfortable boarding for your pet", com.example.vetbook.R.drawable.services),
        ServiceCategory("cat_groom", "Pet Grooming", "Professional grooming services", com.example.vetbook.R.drawable.services),
        ServiceCategory("cat_train", "Pet Training", "Training sessions for your pet", com.example.vetbook.R.drawable.services)
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