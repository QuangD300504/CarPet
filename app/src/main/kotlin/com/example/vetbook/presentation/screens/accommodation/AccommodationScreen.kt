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
        // Type-B header with search bar slot and trailing list/map toggle
        SimpleTopBar(
            title       = uiState.selectedCategory?.displayName ?: "Accommodation",
            onBackClick = onBackClick,
            trailingContent = {
                IconButton(onClick = { viewModel.toggleViewMode() }) {
                    Icon(
                        imageVector = if (uiState.viewMode == ViewMode.LIST) {
                            Icons.Default.Map
                        } else {
                            Icons.AutoMirrored.Filled.List
                        },
                        contentDescription = if (uiState.viewMode == ViewMode.LIST) "Map View" else "List View",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            searchBar = {
                OutlinedTextField(
                    value         = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder   = { Text("Hospital, Coffee shop…") },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor      = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor    = Color.Transparent
                    ),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true
                )
            }
        )

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryFilterChip(
                category   = AccommodationCategory.HOMESTAY,
                isSelected = uiState.selectedCategory == AccommodationCategory.HOMESTAY,
                onClick    = { viewModel.filterByCategory(AccommodationCategory.HOMESTAY) }
            )
            CategoryFilterChip(
                category   = AccommodationCategory.APART,
                isSelected = uiState.selectedCategory == AccommodationCategory.APART,
                onClick    = { viewModel.filterByCategory(AccommodationCategory.APART) }
            )
            CategoryFilterChip(
                category   = AccommodationCategory.COFFEE,
                isSelected = uiState.selectedCategory == AccommodationCategory.COFFEE,
                onClick    = { viewModel.filterByCategory(AccommodationCategory.COFFEE) }
            )
            CategoryFilterChip(
                category   = AccommodationCategory.HOTEL,
                isSelected = uiState.selectedCategory == AccommodationCategory.HOTEL,
                onClick    = { viewModel.filterByCategory(AccommodationCategory.HOTEL) }
            )
        }

        // Content area
        when (uiState.viewMode) {
            ViewMode.LIST -> {
                if (uiState.isLoading) {
                    Box(
                        modifier         = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brand)
                    }
                } else {
                    val error = uiState.error
                    if (error != null) {
                        Box(
                            modifier         = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = error, color = Error)
                        }
                    } else {
                        LazyColumn(
                            modifier        = Modifier.fillMaxSize().weight(1f),
                            contentPadding  = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Default.Map,
                            contentDescription = "Map",
                            modifier           = Modifier.size(64.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text       = "Map View",
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = "Google Maps integration coming soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
