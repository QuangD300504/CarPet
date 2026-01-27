package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.vetbook.presentation.components.profile.MenuItemComponent
import com.example.vetbook.presentation.models.ProfileUiState
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditProfileClick = onEditProfileClick,
        onNotificationClick = onNotificationClick,
        onLanguageClick = onLanguageClick,
        onContactUsClick = onContactUsClick,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onLogout = onLogout
    )
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf(uiState.selectedLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(
                    Color(0xFFFFD813),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        bottomStart = 163.dp,
                        bottomEnd = 163.dp
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Image(
                        painter = painterResource(R.drawable.pawns),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                    IconButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(46.dp)
                            .background(Color(0xFFCBAC10), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.user?.name ?: "PHÙNG CANH MỘ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${uiState.user?.email ?: "mail@gmail.com"} | ${uiState.user?.phoneNumber ?: "+01 234 567 89"}",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 15.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        MenuItemComponent(
                            icon = Icons.Default.Person,
                            label = "Edit profile information",
                            onClick = onEditProfileClick
                        )

                        MenuItemComponent(
                            icon = Icons.Default.Notifications,
                            label = "Notifications",
                            onClick = onNotificationClick,
                            trailingContent = {
                                Text(
                                    text = if (notificationsEnabled) "ON" else "OFF",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1573FE),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        )

                        MenuItemComponent(
                            icon = Icons.Default.Language,
                            label = "Language",
                            onClick = onLanguageClick,
                            trailingContent = {
                                Text(
                                    text = selectedLanguage,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1573FE),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        MenuItemComponent(
                            icon = Icons.Default.Person,
                            label = "Edit profile information",
                            onClick = onEditProfileClick
                        )

                        MenuItemComponent(
                            icon = Icons.Default.Notifications,
                            label = "Notifications",
                            onClick = onNotificationClick
                        )

                        MenuItemComponent(
                            icon = Icons.Default.Language,
                            label = "Language",
                            onClick = onLanguageClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    MenuItemComponent(
                        icon = Icons.Default.Person,
                        label = "Edit profile information",
                        onClick = onEditProfileClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val sampleState = ProfileUiState(
        user = com.example.vetbook.domain.models.User(
            id = "user_1",
            name = "PHÙNG CANH MỘ",
            email = "mail@gmail.com",
            phoneNumber = "+01 234 567 89",
            points = 120,
            profileImage = com.example.vetbook.R.drawable.pawns
        ),
        pets = emptyList(),
        isLoading = false
    )
    PreviewNavScaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ProfileScreenContent(uiState = sampleState)
        }
    }
}

