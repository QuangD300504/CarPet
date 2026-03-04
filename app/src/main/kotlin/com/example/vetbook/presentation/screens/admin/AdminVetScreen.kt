package com.example.vetbook.presentation.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.admin.AdminVetListViewModel

@Composable
fun AdminVetScreen(
    viewModel: AdminVetListViewModel = hiltViewModel(),
    onAddVetClick: () -> Unit,
    onEditVetClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVetClick,
                containerColor = Brand,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Veterinarian")
            }
        },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Brand
                )
            } else if (uiState.veterinarians.isEmpty()) {
                Text(
                    text = "No veterinarians found.\nTap + to add one.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.veterinarians) { vet ->
                        AdminVetItem(
                            vet = vet,
                            onEditClick = { onEditVetClick(vet.id) },
                            onDeleteClick = { viewModel.deleteVet(vet.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVetItem(
    vet: Veterinarian,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vet Image
            AsyncImage(
                model = if (!vet.imageUrl.isNullOrBlank()) vet.imageUrl else "https://via.placeholder.com/100",
                contentDescription = vet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Vet Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vet.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = vet.specialty,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Exp: ${vet.experience}",
                    fontSize = 12.sp,
                    color = Brand
                )
            }

            // Actions
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Brand)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
