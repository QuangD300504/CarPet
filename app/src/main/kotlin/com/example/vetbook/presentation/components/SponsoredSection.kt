package com.example.vetbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SponsoredSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Sponsored",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Horizontal scroll with 6 items from Figma
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(6) {
                SponsoredCard()
            }
        }
    }
}

@Composable
fun SponsoredCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(148.dp)
            .height(242.dp)
            .background(
                color = Color(0xFFD9D9D9), // Grey from Figma
                shape = RoundedCornerShape(15.dp)
            )
    )
}

