package com.example.vetbook.presentation.screens.profile

    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.Help
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
    import coil3.compose.AsyncImage
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.foundation.layout.Arrangement
    import androidx.hilt.navigation.compose.hiltViewModel
    import com.example.vetbook.R
    import com.example.vetbook.presentation.components.profile.MenuItemComponent
    import com.example.vetbook.presentation.models.ProfileUiState
    import com.example.vetbook.presentation.previews.PreviewNavScaffold
    import com.example.vetbook.presentation.theme.Brand
    import com.example.vetbook.presentation.theme.Error
    import com.example.vetbook.presentation.theme.Link
    import com.example.vetbook.presentation.viewmodels.ProfileViewModel
    import androidx.compose.ui.graphics.Brush
    import com.example.vetbook.presentation.theme.HealthPrimary
    import com.example.vetbook.presentation.theme.HealthSurface
    import com.example.vetbook.presentation.theme.HealthMuted
    import androidx.compose.material.icons.filled.Pets
    import com.example.vetbook.presentation.components.common.VetBookImage
    import com.example.vetbook.presentation.viewmodels.SharedNotificationViewModel
    import androidx.compose.ui.platform.LocalContext

    @Composable
    fun ProfileScreen(
        viewModel: ProfileViewModel = hiltViewModel(),
        sharedNotificationViewModel: SharedNotificationViewModel = hiltViewModel(),
        onBackClick: () -> Unit = {},
        avatarOverride: Any? = null,
        onAvatarClick: () -> Unit = {},
        onEditProfileClick: () -> Unit = {},
        onNotificationClick: () -> Unit = {},
        onOrderHistoryClick: () -> Unit = {},
        onLanguageClick: () -> Unit = {},
        onSecurityClick: () -> Unit = {},
        onHelpAndSupportClick: () -> Unit = {},
        onContactUsClick: () -> Unit = {},
        onPrivacyPolicyClick: () -> Unit = {},
        onLogout: () -> Unit = {}
    ) {
        val uiState by viewModel.uiState.collectAsState()
        ProfileScreenContent(
            uiState = uiState,
            sharedNotificationViewModel = sharedNotificationViewModel,
            onBackClick = onBackClick,
            avatarOverride = avatarOverride,
            onAvatarClick = onAvatarClick,
            onEditProfileClick = onEditProfileClick,
            onNotificationClick = onNotificationClick,
            onNotificationsToggle = { enabled -> viewModel.setNotificationsEnabled(enabled) },
            onOrderHistoryClick = onOrderHistoryClick,
            onLanguageClick = onLanguageClick,
            onSecurityClick = onSecurityClick,
            onHelpAndSupportClick = onHelpAndSupportClick,
            onContactUsClick = onContactUsClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onLogout = onLogout
        )
    }

    @Composable
    fun ProfileScreenContent(
        uiState: ProfileUiState,
        sharedNotificationViewModel: SharedNotificationViewModel? = null,
        onBackClick: () -> Unit = {},
        avatarOverride: Any? = null,
        onAvatarClick: () -> Unit = {},
        onEditProfileClick: () -> Unit = {},
        onNotificationClick: () -> Unit = {},
        onNotificationsToggle: (Boolean) -> Unit = {},
        onOrderHistoryClick: () -> Unit = {},
        onLanguageClick: () -> Unit = {},
        onSecurityClick: () -> Unit = {},
        onHelpAndSupportClick: () -> Unit = {},
        onContactUsClick: () -> Unit = {},
        onPrivacyPolicyClick: () -> Unit = {},
        onLogout: () -> Unit = {}
    ) {
        var notificationsEnabled by remember(uiState.notificationsEnabled) {
            mutableStateOf(uiState.notificationsEnabled)
        }
        var selectedLanguage by remember { mutableStateOf(uiState.selectedLanguage) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero Header ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(HealthPrimary, HealthPrimary.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        VetBookImage(
                            model = avatarOverride ?: uiState.user?.profileImageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(100.dp)
                                .border(
                                    width = 3.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            shape = CircleShape,
                            fallbackIcon = Icons.Default.Person,
                            fallbackIconSize = 48.dp
                        )
                        IconButton(
                            onClick = onAvatarClick,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Edit Avatar",
                                tint = HealthPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.user?.name ?: "Người dùng VetBook",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = uiState.user?.email ?: "user@vetbook.com",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Stats Summary Row ────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-20.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.pets.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthPrimary
                        )
                        Text(text = "Thú cưng", fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFEEEEEE))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.upcomingAppointmentCount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPrimary
                    )
                    Text(text = "Sắp tới", fontSize = 12.sp, color = Color.Gray)
                }
                    
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFEEEEEE))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "Thành viên", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Cài đặt tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
                    // Account settings card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            MenuItemComponent(
                                icon = Icons.Default.Person,
                                label = "Thông tin cá nhân",
                                onClick = onEditProfileClick
                            )

                            MenuItemComponent(
                                icon = Icons.Default.Notifications,
                                label = "Thông báo",
                                onClick = onNotificationClick,
                                trailingContent = {
                                    Switch(
                                        checked = notificationsEnabled,
                                        onCheckedChange = { enabled ->
                                            notificationsEnabled = enabled
                                            onNotificationsToggle(enabled)
                                            if (enabled) {
                                                sharedNotificationViewModel?.subscribeToPushWithPermission(
                                                context as androidx.activity.ComponentActivity
                                            )
                                            } else {
                                                sharedNotificationViewModel?.unsubscribeFromPush()
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = HealthPrimary,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFE0E0E0)
                                        )
                                    )
                                }
                            )

                            MenuItemComponent(
                                icon = Icons.Default.ShoppingBag,
                                label = "Lịch sử đơn hàng",
                                onClick = onOrderHistoryClick
                            )

                            MenuItemComponent(
                                icon = Icons.Default.Language,
                                label = "Ngôn ngữ",
                                onClick = onLanguageClick,
                                trailingContent = {
                                    Text(
                                        text = selectedLanguage,
                                        fontSize = 13.sp,
                                        color = HealthPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }

                    Text(
                        text = "Hỗ trợ & Bảo mật",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    // Security / Support card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            MenuItemComponent(
                                icon = Icons.Default.Security,
                                label = "Bảo mật tài khoản",
                                onClick = onSecurityClick
                            )

                            MenuItemComponent(
                                icon = Icons.AutoMirrored.Filled.Help,
                                label = "Trung tâm trợ giúp",
                                onClick = onHelpAndSupportClick
                            )

                            MenuItemComponent(
                                icon = Icons.Default.Phone,
                                label = "Liên hệ với chúng tôi",
                                onClick = onContactUsClick
                            )

                            MenuItemComponent(
                                icon = Icons.Default.PrivacyTip,
                                label = "Chính sách bảo mật",
                                onClick = onPrivacyPolicyClick
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF1F2),
                            contentColor = Color(0xFFE11D48)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng xuất",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                profileImageUrl = null,
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