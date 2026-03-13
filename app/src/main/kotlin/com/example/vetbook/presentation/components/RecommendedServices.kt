package com.example.vetbook.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.TextPrimary

@Composable
fun RecommendedServices(
    categories: List<ServiceCategory>,
    onCategoryClick: (ServiceCategory) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gợi ý cho bạn",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Xem thêm",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HealthPrimary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = categories.take(4),
                key = { it.id }
            ) { category ->
                ServiceCard(
                    category = category,
                    modifier = Modifier.width(280.dp),
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}
