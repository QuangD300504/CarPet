package com.example.vetbook.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.domain.models.ServiceCategory

@Composable
fun RecommendedServices(
    categories: List<ServiceCategory>,
    onCategoryClick: (ServiceCategory) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recommended Service",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSeeAllClick() }
            ) {
                Text(
                    text = "View all",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF0000FF), // Blue from Figma
                )
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = Color(0xFF0000FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scroll from Figma
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(19.dp)
        ) {
            items(
                items = categories.take(4),
                key = { it.id }
            ) { category ->
                ServiceCard(
                    category = category,
                    modifier = Modifier.width(316.dp),
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}
