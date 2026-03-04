package com.example.vetbook.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.Link

@Composable
fun ServiceCategoriesSection(
    categories: List<ServiceCategory>,
    onCategoryClick: (ServiceCategory) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Service Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onViewAllClick() }
            ) {
                Text(
                    text = "View all",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Link
                )
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = Link
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 2x2 Grid layout instead of horizontal scroll
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val displayCategories = categories.take(4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                displayCategories.take(2).forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                displayCategories.drop(2).take(2).forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(76.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F2F2) // Light grey background from Figma
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
                Icon(
                    imageVector = com.example.vetbook.presentation.utils.getCategoryIcon(category.id),
                    contentDescription = category.title,
                    modifier = Modifier.size(32.dp),
                    tint = Brand // Brand gold icon
                )
            }
            Text(
                text = category.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

