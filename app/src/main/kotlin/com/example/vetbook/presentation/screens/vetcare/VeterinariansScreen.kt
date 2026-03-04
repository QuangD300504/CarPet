package com.example.vetbook.presentation.screens.vetcare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.R
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.models.VeterinariansUiState
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.Error
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel

@Composable
fun VeterinariansScreen(
    viewModel: VeterinariansViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVetClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    VeterinariansContent(
        uiState     = uiState,
        onBackClick = onBackClick,
        onVetClick  = onVetClick
    )
}

@Composable
fun VeterinariansContent(
    uiState: VeterinariansUiState,
    onBackClick: () -> Unit,
    onVetClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredVets = remember(uiState.veterinarians, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) uiState.veterinarians
        else uiState.veterinarians.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.specialty.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Type-B header with inline search bar slot
        SimpleTopBar(
            title       = "Veterinary Care",
            onBackClick = onBackClick,
            searchBar   = {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search doctor, specialty…") },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier   = Modifier.fillMaxWidth(),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor      = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor    = Color.Transparent
                    ),
                    shape      = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        )

        // Content
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, color = Error)
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text      = "Top Rated Doctors",
                        style     = MaterialTheme.typography.titleLarge,
                        modifier  = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(items = filteredVets, key = { it.id }) { vet ->
                    TopRateDoctorCard(vet, onClick = { onVetClick(vet.id) })
                }
            }
        }
    }
}

@Composable
fun NavigationIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier         = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Brand,
                modifier           = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TopRateDoctorCard(vet: Veterinarian, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter            = painterResource(R.drawable.pawns),
                contentDescription = null,
                modifier           = Modifier.size(64.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = vet.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = vet.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = "10:30 AM – 3:30 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text      = "Fee: \$25.00",
                    style     = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment  = Alignment.End,
                verticalArrangement  = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Star,
                        contentDescription = null,
                        tint               = Brand,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text      = vet.rating,
                        style     = MaterialTheme.typography.titleMedium
                    )
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VeterinariansScreenPreview() {
    VeterinariansContent(
        uiState = VeterinariansUiState(
            veterinarians = listOf(
                Veterinarian("1", "Dr. Hamza Tariq", "Senior Surgeon", "10 years", rating = "4.9", reviewsCount = 12, initials = "DHT"),
                Veterinarian("2", "Dr. Alina Fatima", "Cardiologist", "8 years", rating = "5.0", reviewsCount = 15, initials = "DAF")
            )
        ),
        onBackClick = {},
        onVetClick  = {}
    )
}
