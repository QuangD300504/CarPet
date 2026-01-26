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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.vetbook.presentation.models.VeterinariansUiState
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel

@Composable
fun VeterinariansScreen(
    viewModel: VeterinariansViewModel = hiltViewModel(),
    onVetClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    VeterinariansContent(
        uiState = uiState,
        onVetClick = onVetClick
    )
}

@Composable
fun VeterinariansContent(
    uiState: VeterinariansUiState,
    onVetClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Yellow top section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEB3B))
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Q Doctor, Hospital...") },
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // SOS Button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935) // Red
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigation icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavigationIcon(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onClick = { }
                )
                NavigationIcon(
                    icon = Icons.Default.Business,
                    label = "Hospital",
                    onClick = { }
                )
                NavigationIcon(
                    icon = Icons.Default.Person,
                    label = "Online",
                    onClick = { }
                )
            }
        }
        
        // White content area
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.error, color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Top Rate Doctor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(
                    items = uiState.veterinarians,
                    key = { it.id }
                ) { vet ->
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
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFFFEB3B),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun TopRateDoctorCard(vet: Veterinarian, onClick: () -> Unit) {
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
            // Profile picture
            Image(
                painter = painterResource(R.drawable.pawns), // Using placeholder
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Doctor info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vet.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vet.specialty,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "10:30 AM-3:30 PM", // Availability - could be added to model
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                val feeText = remember(vet.reviewsCount) {
                    "Fee: \$${vet.reviewsCount}"
                }
                Text(
                    text = feeText, // Using reviewsCount as fee placeholder
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            // Rating and arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = vet.rating,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
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
                Veterinarian("1", "Dr. Hamza Tariq", "Senior Surgeon", "10 years experience", rating = "4.9", reviewsCount = 12, initials = "DHT"),
                Veterinarian("2", "Dr. Alina Fatima", "Cardiologist", "8 years experience", rating = "5.0", reviewsCount = 15, initials = "DAF")
            )
        ),
        onVetClick = {}
    )
}
