package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun LanguageScreen(
    onBackClick: () -> Unit = {},
    onLanguageSelected: (String) -> Unit = {}
) {
    var selectedLanguage by remember { mutableStateOf("English (US)") }
    
    val suggestedLanguages = listOf("English (US)", "English (UK)")
    val otherLanguages = listOf(
        "Mandarin", "Hindi", "Spanish", "French",
        "Arabic", "Russian", "Indonesia", "Vietnamese"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Yellow header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEB3B))
                .padding(16.dp),
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
                text = "edit language",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Language",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Text(
                    text = "Suggested",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(
                items = suggestedLanguages,
                key = { it }
            ) { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = {
                        selectedLanguage = language
                        onLanguageSelected(language)
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Text(
                    text = "Others",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(
                items = otherLanguages,
                key = { it }
            ) { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = {
                        selectedLanguage = language
                        onLanguageSelected(language)
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF2196F3)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LanguageScreenPreview() {
    LanguageScreen()
}

