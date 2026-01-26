package com.example.vetbook.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.viewmodels.ProfileViewModel

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var fullName by remember { mutableStateOf(uiState.user?.name ?: "Phùng Canh Mộ") }
    var nickName by remember { mutableStateOf("puerto_rico") }
    var email by remember { mutableStateOf(uiState.user?.email ?: "youremail@domain.com") }
    var phoneNumber by remember { mutableStateOf("123-456-7890") }
    var country by remember { mutableStateOf("United States") }
    var gender by remember { mutableStateOf("Female") }
    var address by remember { mutableStateOf("45 New Avenue, New York") }
    
    var showCountryDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    
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
                text = "edit profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
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
            
            // Full name
            ProfileTextField(
                label = "Full name",
                value = fullName,
                onValueChange = { fullName = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nick name
            ProfileTextField(
                label = "Nick name",
                value = nickName,
                onValueChange = { nickName = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email
            ProfileTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone Number
            ProfileTextField(
                label = "Phone Number",
                value = phoneNumber,
                onValueChange = { phoneNumber = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Country dropdown
            ProfileDropdownField(
                label = "Country",
                value = country,
                onClick = { showCountryDropdown = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gender dropdown
            ProfileDropdownField(
                label = "Gender",
                value = gender,
                onClick = { showGenderDropdown = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Address
            ProfileTextField(
                label = "Address",
                value = address,
                onValueChange = { address = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Submit button
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
    EditProfileScreen()
}

