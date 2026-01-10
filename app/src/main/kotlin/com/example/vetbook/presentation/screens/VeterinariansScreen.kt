package com.example.vetbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.models.VeterinariansUiState
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel

@Composable
fun VeterinariansScreen(
    viewModel: VeterinariansViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVetClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    VeterinariansContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onVetClick = onVetClick
    )
}

@Composable
fun VeterinariansContent(
    uiState: VeterinariansUiState,
    onBackClick: () -> Unit,
    onVetClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFF9800))
        } else if (uiState.error != null) {
            Text(text = uiState.error, modifier = Modifier.align(Alignment.Center), color = Color.Red)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.veterinarians) { vet ->
                    VeterinarianCard(vet, onClick = { onVetClick(vet.id) })
                }
            }
        }
    }
}

@Composable
fun VeterinarianCard(vet: Veterinarian, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vet.initials,
                    color = Color(0xFFFFB74D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = vet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 1f)
                )
                Text(
                    text = vet.specialty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray.copy(alpha = 1f)
                )
                Text(
                    text = vet.experience,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = vet.rating,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${vet.reviewsCount} reviews)",
                        fontSize = 14.sp,
                        color = Color.Gray.copy(alpha = 1f)
                    )
                }
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
                Veterinarian("1", "Dr. Sarah Johnson", "Small Animal Medicine", "12 years experience", initials = "DSJ"),
                Veterinarian("2", "Dr. Michael Chen", "Surgery & Emergency Care", "15 years experience", initials = "DMC"),
                Veterinarian("3", "Trương Tuấn Tú", "Exotic Animals", "8 years experience", initials = "DER"),
                Veterinarian("4", "Dr. David Thompson", "Dental Care", "10 years experience", initials = "DDT")
            )
        ),
        onBackClick = {},
        onVetClick = {}
    )
}
