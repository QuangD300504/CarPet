package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pre-fill from Firebase profile; updates if profile loads after composition.
    var fullName by remember(uiState.user?.name) {
        mutableStateOf(uiState.user?.name ?: "")
    }
    var phone by remember(uiState.user?.phoneNumber) {
        mutableStateOf(uiState.user?.phoneNumber ?: "")
    }

    // Email is read-only — changing email requires re-authentication in Firebase.
    val email = uiState.user?.email ?: ""

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Full name
            LabeledField(label = "Họ và tên") {
                CustomTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Nguyễn Văn A",
                    imeAction = ImeAction.Next
                )
            }

            // Email — disabled, Firebase doesn't allow direct email change
            LabeledField(label = "Email") {
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null,
                            tint = HealthMuted, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        Text(
                            text = "Không thể đổi",
                            fontSize = 11.sp,
                            color = HealthMuted,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFFF1F5F9),
                        disabledBorderColor = Color.Transparent,
                        disabledTextColor = HealthMuted
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Phone
            LabeledField(label = "Số điện thoại") {
                CustomTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "0912 345 678",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveProfile(fullName, phone) { success, message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                        if (success) onSubmitClick()
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        content()
    }
}