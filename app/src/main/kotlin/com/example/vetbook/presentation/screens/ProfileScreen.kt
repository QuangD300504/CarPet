package com.example.vetbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.profile.MenuItemComponent
import com.example.vetbook.presentation.components.profile.PetCard
import com.example.vetbook.presentation.components.profile.PointsCard
import com.example.vetbook.presentation.components.profile.ProfileHeaderCard
import com.example.vetbook.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onPetClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Profile Header Card
        uiState.user?.let { user ->
            // Use user data from Firebase
            val initials = if (user.name.contains(" ")) {
                user.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").uppercase()
            } else {
                user.name.take(2).uppercase()
            }

            ProfileHeaderCard(
                name = user.name,
                email = user.email,
                initials = initials
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Points Card
            PointsCard(
                points = user.points,
                pointsLabel = "VetBook Rewards"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // My Pets Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Pets",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Add Pet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Pet List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.pets.forEach { pet ->
                    val petEmoji = if (pet.type.lowercase() == "dog") "🐕" else "😸"
                    PetCard(
                        name = pet.name,
                        breed = pet.breed,
                        petEmoji = petEmoji,
                        onClick = onPetClick,
                        petId = pet.id
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Menu Section
            MenuItemComponent(icon = Icons.Default.Settings, label = "My Bookings")
            Spacer(modifier = Modifier.height(12.dp))
            MenuItemComponent(icon = Icons.Default.Notifications, label = "Notifications")
            Spacer(modifier = Modifier.height(12.dp))
            MenuItemComponent(
                icon = Icons.Default.Translate,
                label = "Language",
                trailingContent = {
                    Text(text = uiState.selectedLanguage, fontSize = 14.sp, color = Color(0xFF999999))
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuItemComponent(
                icon = Icons.Default.Brightness4,
                label = "Dark Mode",
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkModeEnabled,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFF9800),
                            checkedTrackColor = Color(0xFFFFB74D),
                            uncheckedThumbColor = Color(0xFFDDDDDD),
                            uncheckedTrackColor = Color(0xFFEEEEEE)
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Log Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
