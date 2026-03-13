package com.example.vetbook.presentation.screens.accommodation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.accommodation.AccommodationCard
import com.example.vetbook.presentation.components.accommodation.CategoryFilterChip
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.ViewMode
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.Error
import com.example.vetbook.presentation.viewmodels.AccommodationViewModel

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.Background

@Composable
fun AccommodationScreen(
    viewModel: AccommodationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onAccommodationClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Premium Top Bar
        SimpleTopBar(
            title       = "Lưu trú",
            onBackClick = onBackClick,
            trailingContent = {
                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier.background(HealthSurface, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (uiState.viewMode == ViewMode.LIST) {
                            Icons.Default.Map
                        } else {
                            Icons.AutoMirrored.Filled.List
                        },
                        contentDescription = if (uiState.viewMode == ViewMode.LIST) "Bản đồ" else "Danh sách",
                        tint = HealthPrimary
                    )
                }
            },
            searchBar = {
                OutlinedTextField(
                    value         = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder   = { 
                        Text(
                            "Bệnh viện, Quán cà phê...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        ) 
                    },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = HealthPrimary)
                    },
                    modifier      = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor      = HealthPrimary,
                        unfocusedBorderColor    = HealthSurface,
                        cursorColor = HealthPrimary
                    ),
                    shape         = RoundedCornerShape(16.dp),
                    singleLine    = true
                )
            }
        )

        // Category filter chips - Scrollable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccommodationCategory.values().forEach { category ->
                CategoryFilterChip(
                    category   = category,
                    isSelected = uiState.selectedCategory == category,
                    onClick    = { viewModel.filterByCategory(category) }
                )
            }
        }

        // Content area
        when (uiState.viewMode) {
            ViewMode.LIST -> {
                if (uiState.isLoading) {
                    Box(
                        modifier         = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HealthPrimary)
                    }
                } else {
                    val error = uiState.error
                    if (error != null) {
                        Box(
                            modifier         = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Đã xảy ra lỗi: $error", color = Color.Red)
                        }
                    } else if (uiState.filteredAccommodations.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Không tìm thấy kết quả phù hợp",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier        = Modifier.fillMaxSize().weight(1f),
                            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.filteredAccommodations,
                                key   = { it.id }
                            ) { accommodation ->
                                AccommodationCard(
                                    accommodation = accommodation,
                                    onClick       = { onAccommodationClick(accommodation.id) }
                                )
                            }
                        }
                    }
                }
            }
            ViewMode.MAP -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(HealthSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Default.Map,
                            contentDescription = "Bản đồ",
                            modifier           = Modifier.size(80.dp),
                            tint               = HealthPrimary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text       = "Chế độ Bản đồ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text  = "Tính năng Bản đồ đang được phát triển",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccommodationScreenPreview() {
    AccommodationScreen()
}
