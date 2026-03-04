package com.example.vetbook.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.R
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.presentation.viewmodels.PetProfileViewModel
import com.example.vetbook.presentation.theme.Brand

@Composable
fun PetProfileScreen(
    petId: String,
    viewModel: PetProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Brand
                )
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Failed to load pet",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }

            uiState.pet != null -> {
                PetProfileContent(pet = uiState.pet!!, onBackClick = onBackClick)
            }
        }
    }
}

@Composable
private fun PetProfileContent(
    pet: Pet,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top image section (placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.pawns),
                contentDescription = pet.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(pet.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(text = pet.type + if (pet.age.isNotBlank()) " · ${pet.age}" else "", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(16.dp))

            Section(title = "About ${pet.name}") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox(label = "Weight", value = pet.weight.ifBlank { "-" })
                    StatBox(label = "Type", value = pet.type)
                    StatBox(label = "Breed", value = pet.breed)
                }
                Spacer(Modifier.height(12.dp))
                if (pet.note.isNotBlank()) {
                    Text(pet.note, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun RowScope.StatBox(label: String, value: String) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
