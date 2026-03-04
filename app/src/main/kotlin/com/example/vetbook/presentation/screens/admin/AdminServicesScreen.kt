package com.example.vetbook.presentation.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.Brand

/**
 * Admin Services screen — shows all "Recommended Services" packages.
 * The existing data is hardcoded / local; a Firestore-backed version
 * can be wired up later. This screen acts as a placeholder/viewer for now
 * and lets admin read the list with a clear note to extend later.
 */
@Composable
fun AdminServicesScreen() {
    val services = listOf(
        "Pet Hotel & Boarding",
        "Spa & Grooming",
        "Dog Walking",
        "Veterinary Consultation",
        "Pet Taxi",
        "Training",
        "Photography"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ℹ️  Services are currently seeded from local data. " +
                        "To enable full Firestore management, migrate ServiceCategory " +
                        "to a Firestore collection and extend ServiceRepository.",
                fontSize = 12.sp,
                color = Color(0xFF92400E),
                modifier = Modifier.padding(12.dp)
            )
        }

        Text("Current Service Categories", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)

        services.forEach { svc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(svc, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("Manage →", fontSize = 12.sp, color = Brand)
                }
            }
        }
    }
}
