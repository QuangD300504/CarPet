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
import com.example.vetbook.presentation.components.ServiceCard
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
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
    
    // Vietnamese display names
    val vnDisplayName = when(category) {
        AccommodationCategory.HOMESTAY -> "Homestay"
        AccommodationCategory.APART -> "Căn hộ"
        AccommodationCategory.COFFEE -> "Cà phê"
        AccommodationCategory.HOTEL -> "Khách sạn"
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = vnDisplayName,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = vnDisplayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HealthPrimary,
            selectedLabelColor     = Color.White,
            containerColor         = HealthSurface,
            labelColor             = TextSecondary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(40.dp)
    )
}

