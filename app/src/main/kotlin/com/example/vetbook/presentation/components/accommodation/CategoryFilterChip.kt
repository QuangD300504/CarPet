package com.example.vetbook.presentation.components.accommodation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.theme.Brand

@Composable
fun CategoryFilterChip(
    category: AccommodationCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (category) {
        AccommodationCategory.HOMESTAY -> Icons.Default.Home
        AccommodationCategory.APART -> Icons.Default.Apartment
        AccommodationCategory.COFFEE -> Icons.Default.LocalCafe
        AccommodationCategory.HOTEL -> Icons.Default.Bed
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.displayName,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = category.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Brand,
            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
            containerColor         = MaterialTheme.colorScheme.surfaceVariant,
            labelColor             = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled             = true,
            selected            = isSelected,
            borderColor         = if (isSelected) Brand else androidx.compose.ui.graphics.Color.Transparent,
            selectedBorderColor = Brand
        ),
        modifier = modifier
    )
}

