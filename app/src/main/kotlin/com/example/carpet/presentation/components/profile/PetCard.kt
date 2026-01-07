package com.example.carpet.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable Pet Card component for the Profile Screen
 */
@Composable
fun PetCard(
    name: String,
    breed: String,
    petEmoji: String
) {
    Column(
        modifier = Modifier
            .background(
                color = Color(0xFFFAFAFA),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .then(Modifier.size(width = 160.dp, height = 180.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Pet emoji as avatar
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = petEmoji, fontSize = 40.sp)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Pet name
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Pet breed
        Text(
            text = breed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF999999)
        )
    }
}
