package com.example.vetbook.presentation.screens.accommodation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.accommodation.AccommodationCard
import com.example.vetbook.presentation.components.accommodation.CategoryFilterChip
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.ViewMode
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
            .background(Color.White)
    ) {
        // Yellow header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEB3B))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                
                Text(
                    text = uiState.selectedCategory?.displayName ?: "Accommodation",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // View toggle button
                IconButton(onClick = { viewModel.toggleViewMode() }) {
                    Icon(
                        imageVector = if (uiState.viewMode == ViewMode.LIST) {
                            Icons.Default.Map
                        } else {
                            Icons.Default.List
                        },
                        contentDescription = if (uiState.viewMode == ViewMode.LIST) "Map View" else "List View",
                        tint = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Hospital, Coffee shop") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        // Category filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryFilterChip(
                category = AccommodationCategory.HOMESTAY,
                isSelected = uiState.selectedCategory == AccommodationCategory.HOMESTAY,
                onClick = { viewModel.filterByCategory(AccommodationCategory.HOMESTAY) }
            )
            CategoryFilterChip(
                category = AccommodationCategory.APART,
                isSelected = uiState.selectedCategory == AccommodationCategory.APART,
                onClick = { viewModel.filterByCategory(AccommodationCategory.APART) }
            )
            CategoryFilterChip(
                category = AccommodationCategory.COFFEE,
                isSelected = uiState.selectedCategory == AccommodationCategory.COFFEE,
                onClick = { viewModel.filterByCategory(AccommodationCategory.COFFEE) }
            )
            CategoryFilterChip(
                category = AccommodationCategory.HOTEL,
                isSelected = uiState.selectedCategory == AccommodationCategory.HOTEL,
                onClick = { viewModel.filterByCategory(AccommodationCategory.HOTEL) }
            )
        }
        
        // Content area
        when (uiState.viewMode) {
            ViewMode.LIST -> {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF9800))
                    }
                } else {
                    val error = uiState.error
                    if (error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error,
                                color = Color.Red
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = uiState.filteredAccommodations,
                                key = { it.id }
                            ) { accommodation ->
                                AccommodationCard(
                                    accommodation = accommodation,
                                    onClick = { onAccommodationClick(accommodation.id) }
                                )
                            }
                        }
                    }
                }
            }
            ViewMode.MAP -> {
                // Placeholder map view
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Map View",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Google Maps integration coming soon",
                            fontSize = 14.sp,
                            color = Color.Gray
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

