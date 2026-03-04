package com.example.vetbook.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalHotel
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(categoryId: String): ImageVector {
    return when(categoryId) {
        "cat_vet" -> Icons.Default.LocalHospital
        "cat_hotel" -> Icons.Default.LocalHotel
        "cat_ride" -> Icons.Default.LocalTaxi
        "cat_spa" -> Icons.Default.Spa
        "cat_training" -> Icons.Default.School
        "cat_party" -> Icons.Default.Celebration
        "cat_funeral" -> Icons.Default.LocalFlorist
        else -> Icons.Default.Category
    }
}
