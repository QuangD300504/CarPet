package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.viewmodels.ProfileViewModel
import com.example.vetbook.presentation.previews.PreviewNavScaffold
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    EditProfileContent(
        fullNameInitial = uiState.user?.name ?: "Phùng Canh Mộ",
        nickNameInitial = "puerto_rico",
        emailInitial = uiState.user?.email ?: "youremail@domain.com",
        phoneNumberInitial = uiState.user?.phoneNumber ?: "123-456-7890",
        countryInitial = "United States",
        genderInitial = "Female",
        addressInitial = "45 New Avenue, New York",
        onBackClick = onBackClick,
        onSubmitClick = onSubmitClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    fullNameInitial: String,
    nickNameInitial: String,
    emailInitial: String,
    phoneNumberInitial: String,
    countryInitial: String,
    genderInitial: String,
    addressInitial: String,
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf(fullNameInitial) }
    var nickName by remember { mutableStateOf(nickNameInitial) }
    var email by remember { mutableStateOf(emailInitial) }
    var phoneNumber by remember { mutableStateOf(phoneNumberInitial) }
    var country by remember { mutableStateOf(countryInitial) }
    var gender by remember { mutableStateOf(genderInitial) }
    var address by remember { mutableStateOf(addressInitial) }

    var showCountryDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa hồ sơ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ProfileTextField(
                label = "Họ và tên",
                value = fullName,
                onValueChange = { fullName = it },
                icon = Icons.Default.Person
            )

            ProfileTextField(
                label = "Biệt danh",
                value = nickName,
                onValueChange = { nickName = it },
                icon = Icons.Default.AlternateEmail
            )

            ProfileTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                icon = Icons.Default.Email
            )

            ProfileTextField(
                label = "Số điện thoại",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                icon = Icons.Default.Phone
            )

            ProfileDropdownField(
                label = "Quốc gia",
                value = country,
                onClick = { showCountryDropdown = true },
                icon = Icons.Default.Public
            )

            ProfileDropdownField(
                label = "Giới tính",
                value = gender,
                onClick = { showGenderDropdown = true },
                icon = Icons.Default.Wc
            )

            ProfileTextField(
                label = "Địa chỉ",
                value = address,
                onValueChange = { address = it },
                icon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "XÁC NHẬN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(imageVector = icon, contentDescription = null, tint = HealthPrimary, modifier = Modifier.size(20.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = HealthPrimary,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                cursorColor = HealthPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ProfileDropdownField(
    label: String,
    value: String,
    onClick: () -> Unit,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            leadingIcon = {
                Icon(imageVector = icon, contentDescription = null, tint = HealthPrimary, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = HealthPrimary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = HealthPrimary,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    PreviewNavScaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            EditProfileContent(
                fullNameInitial = "Phùng Canh Mộ",
                nickNameInitial = "puerto_rico",
                emailInitial = "youremail@domain.com",
                phoneNumberInitial = "+01 234 567 89",
                countryInitial = "United States",
                genderInitial = "Female",
                addressInitial = "45 New Avenue, New York"
            )
        }
    }
}

