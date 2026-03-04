package com.example.vetbook.presentation.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.AddPetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    viewModel: AddPetViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var typeExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val types = listOf("Dog", "Cat", "Bird", "Other")
    val genders = listOf("Male", "Female")

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage ?: "") }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SimpleTopBar(
            title       = "Add Pet",
            onBackClick = onBackClick
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(
                    value = uiState.name,
                    onValueChange = viewModel::setName,
                    placeholder = "Name",
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                // Type dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.type,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Type", color = Color(0xFF9CA3AF)) },
                        trailingIcon = { Text("▼", color = Color.Black) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .padding(0.dp)
                    ) {
                        // Click overlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(0.dp)
                                .background(Color.Transparent)
                                .let { it },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // no-op
                        }
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .then(Modifier)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .let { it }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .let { it }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .let { it }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .let { it }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .let { it }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(0.dp)
                            .background(Color.Transparent)
                            .clickable { typeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    viewModel.setType(t)
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                CustomTextField(
                    value = uiState.breed,
                    onValueChange = viewModel::setBreed,
                    placeholder = "Breed",
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                // Gender dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.gender,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Gender", color = Color(0xFF9CA3AF)) },
                        trailingIcon = { Text("▼", color = Color.Black) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { genderExpanded = true }
                    )
                    DropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        genders.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    viewModel.setGender(g)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Age + weight
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomTextField(
                        value = uiState.ageYears,
                        onValueChange = viewModel::setAgeYears,
                        modifier = Modifier.weight(1f),
                        placeholder = "Years",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Right) }
                    )
                    CustomTextField(
                        value = uiState.ageMonths,
                        onValueChange = viewModel::setAgeMonths,
                        modifier = Modifier.weight(1f),
                        placeholder = "Months",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }

                CustomTextField(
                    value = uiState.weightKg,
                    onValueChange = viewModel::setWeightKg,
                    placeholder = "Weight (kg)",
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                CustomTextField(
                    value = uiState.note,
                    onValueChange = viewModel::setNote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = "Note",
                    singleLine = false,
                    maxLines = 5,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.save(onSuccess = onSaved) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .height(56.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brand,
                    contentColor   = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.height(18.dp),
                        color       = androidx.compose.ui.graphics.Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
