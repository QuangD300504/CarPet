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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.vetbook.R
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.models.VeterinariansUiState
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.Error
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel
import com.example.vetbook.presentation.components.common.VetBookImage

@Composable
fun VeterinariansScreen(
    viewModel: VeterinariansViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVetClick: (String) -> Unit = {},
    vaccineContextLabel: String? = null,
    onDismissVaccineContext: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    VeterinariansContent(
        uiState     = uiState,
        onBackClick = onBackClick,
        onVetClick  = onVetClick,
        vaccineContextLabel = vaccineContextLabel,
        onDismissVaccineContext = onDismissVaccineContext
    )
}

@Composable
fun VeterinariansContent(
    uiState: VeterinariansUiState,
    onBackClick: () -> Unit,
    onVetClick: (String) -> Unit,
    vaccineContextLabel: String? = null,
    onDismissVaccineContext: () -> Unit = {}
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
            .background(HealthSurface)
    ) {
        // Type-B header with inline search bar slot
        SimpleTopBar(
            title       = "Chăm Sóc Thú Y",
            onBackClick = onBackClick,
            searchBar   = {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Tìm bác sĩ, chuyên khoa...", color = Color.Gray.copy(alpha = 0.6f)) },
                    leadingIcon   = {
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Search",
                            tint = HealthPrimary
                        )
                    },
                    modifier   = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor      = HealthPrimary,
                        unfocusedBorderColor    = Color.LightGray.copy(alpha = 0.3f),
                        cursorColor = HealthPrimary
                    ),
                    shape      = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
        )

        // Content
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HealthPrimary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, color = Error)
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // VAC-01: Show vaccine context banner when booking from vaccine flow
                if (vaccineContextLabel != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE6F4F1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D7377).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Vaccines,
                                    contentDescription = null,
                                    tint = Color(0xFF0D7377),
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Đang đặt lịch tiêm chủng",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0D5E61),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        vaccineContextLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D7377)
                                    )
                                }
                                // VAC-05: dismiss button
                                IconButton(
                                    onClick = onDismissVaccineContext,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hủy chọn vaccine",
                                        tint = Color(0xFF0D7377).copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text      = "Bác Sĩ Hàng Đầu",
                        style     = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color(0xFF2D3142),
                        modifier  = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(items = filteredVets, key = { it.id }) { vet ->
                    TopRateDoctorCard(vet, onClick = { onVetClick(vet.id) })
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
    // 1. Tính toán rating live
    val liveRating = if (vet.reviews.isEmpty()) null
    else vet.reviews.map { it.rating }.average()

    val liveRatingLabel = when {
        vet.reviews.isEmpty() -> "Mới"
        else -> String.format("%.1f", liveRating)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VetBookImage(
                model = vet.imageUrl,
                contentDescription = vet.name,
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(16.dp),
                initials = vet.initials,
                fallbackIcon = Icons.Default.Person,
                fallbackIconSize = 32.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vet.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3142)
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vet.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hàng 1: Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = liveRatingLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2D3142)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${vet.reviews.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hàng 2: Giá phí
                Text(
                    text = "Phí: ${String.format("%,.0f", vet.servicePrice)}đ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthPrimary
                    )
                )
            } 

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Xem chi tiết",
                    tint = HealthPrimary,
                    modifier = Modifier.size(24.dp)
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
                Veterinarian("1", "Dr. Hamza Tariq", "Senior Surgeon", "10 years", rating = 4.9, reviewsCount = 12, initials = "DHT"),
                Veterinarian("2", "Dr. Alina Fatima", "Cardiologist", "8 years", rating = 5.0, reviewsCount = 15, initials = "DAF")
            )
        ),
        onBackClick = {},
        onVetClick  = {}
    )
}