package com.example.carpet.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carpet.data.repository.MockUserRepository
import com.example.carpet.presentation.viewmodels.PetProfileViewModel
import com.example.carpet.presentation.viewmodels.PetProfileViewModelFactory

/**
 * Screen displaying detailed information about a specific pet
 * Shows pet image, info, health status, and vaccination records
 */
@Composable
fun PetProfileScreen(
    petId: String,
    viewModel: PetProfileViewModel = viewModel(
        factory = PetProfileViewModelFactory(MockUserRepository(), petId)
    ),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (uiState.isLoading) {
            Text(
                text = "Loading...",
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.error != null) {
            Text(
                text = "Error: ${uiState.error}",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        } else {
            uiState.pet?.let { pet ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header with back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1A1A1A)
                            )
                        }
                        Text(
                            text = pet.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Box(modifier = Modifier.size(40.dp)) // Spacer for alignment
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pet Image/Emoji Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color(0xFFFAFAFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        val petEmoji = if (pet.type.lowercase() == "dog") "🐕" else "😸"
                        Text(
                            text = petEmoji,
                            fontSize = 140.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pet Info Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                color = Color(0xFFFAFAFA),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        // Breed
                        InfoRow(
                            label = "Breed:",
                            value = pet.breed
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Age
                        if (pet.age.isNotEmpty()) {
                            InfoRow(
                                label = "Age:",
                                value = pet.age
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Gender
                        if (pet.gender.isNotEmpty()) {
                            InfoRow(
                                label = "Gender:",
                                value = pet.gender
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Weight
                        if (pet.weight.isNotEmpty()) {
                            InfoRow(
                                label = "Weight:",
                                value = pet.weight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Health Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Tình trạng sức khỏe",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Parasitic Status Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFFAFAFA),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tình trạng: ${pet.parasiticStatus}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    text = if (pet.parasiticStatus.lowercase() == "healthy" || pet.parasiticStatus.lowercase() == "khỏe mạnh") "✓" else "⚠",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Vaccination Section
                    if (pet.vaccinations.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Tiêm chủng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            pet.vaccinations.forEach { vaccination ->
                                VaccinationItem(
                                    title = vaccination.title,
                                    isCompleted = vaccination.isCompleted,
                                    date = vaccination.date
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Notes Section
                    if (pet.note.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Ghi chú",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFFAFAFA),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = pet.note,
                                    fontSize = 13.sp,
                                    color = Color(0xFF666666),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper component to display info rows with label and value
 */
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF666666)
        )
    }
}

/**
 * Helper component to display vaccination records
 */
@Composable
private fun VaccinationItem(
    title: String,
    isCompleted: Boolean,
    date: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCompleted) Color(0xFFF1F8E9) else Color(0xFFFFF3E0),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            if (date != null) {
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Text(
            text = if (isCompleted) "✓" else "○",
            fontSize = 18.sp,
            color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFF9800),
            fontWeight = FontWeight.Bold
        )
    }
}
