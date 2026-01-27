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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Edit profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileTextField(
                label = "Full name",
                value = fullName,
                onValueChange = { fullName = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                label = "Nick name",
                value = nickName,
                onValueChange = { nickName = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                label = "Phone Number",
                value = phoneNumber,
                onValueChange = { phoneNumber = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileDropdownField(
                label = "Country",
                value = country,
                onClick = { showCountryDropdown = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileDropdownField(
                label = "Gender",
                value = gender,
                onClick = { showGenderDropdown = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                label = "Address",
                value = address,
                onValueChange = { address = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SUBMIT",
                    fontSize = 18.sp,
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
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun ProfileDropdownField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
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

