package com.example.vetbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel

@Composable
fun DoctorProfileScreen(
    doctorId: String,
    viewModel: VeterinariansViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onBookClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val doctor = uiState.veterinarians.find { it.id == doctorId }

    if (doctor != null) {
        DoctorProfileContent(doctor = doctor, onBackClick = onBackClick, onBookClick = onBookClick)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Doctor not found")
        }
    }
}

@Composable
fun DoctorProfileContent(
    doctor: Veterinarian,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Doctor Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = doctor.initials,
                        color = Color(0xFFFFB74D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = doctor.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 1f)
                )
                Text(
                    text = doctor.specialty,
                    fontSize = 16.sp,
                    color = Color.Gray.copy(alpha = 1f)
                )
                Text(
                    text = doctor.experience,
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = doctor.rating,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${doctor.reviewsCount} reviews)",
                        fontSize = 16.sp,
                        color = Color.Gray.copy(alpha = 1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = doctor.bio,
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 1f),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Review Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Leave a Review",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 1f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = "Rating", fontWeight = FontWeight.Medium, color = Color.Black.copy(alpha = 1f))
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Your Review", fontWeight = FontWeight.Medium, color = Color.Black.copy(alpha = 1f))
                
                var reviewText by remember { mutableStateOf("") }
                TextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Share your experience...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Submit Review", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBookClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Book", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Reviews Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Reviews (0)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 1f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No reviews yet. Be the first to review!",
                    color = Color.Gray.copy(alpha = 1f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorProfileScreenPreview() {
    DoctorProfileContent(
        doctor = Veterinarian(
            id = "3",
            name = "Trương Tuấn Tú",
            specialty = "Exotic Animals",
            experience = "36 years experience",
            initials = "DER",
            bio = "Expert in treating birds, reptiles, and other exotic pets with gentle and specialized care.",
            reviewsCount = 36
        ),
        onBackClick = {},
        onBookClick = {}
    )
}
