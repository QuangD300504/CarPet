package com.example.vetbook.presentation.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import coil3.compose.AsyncImage
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.components.topbars.SimpleTopBar
import com.example.vetbook.presentation.components.pets.VaccineReviewModal
import com.example.vetbook.utils.compressImageForAvatar
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.viewmodels.AddPetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    petId: String? = null,
    viewModel: AddPetViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSaved: (isEdit: Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var showDatePicker by remember { mutableStateOf(false) }
val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = uiState.birthDateMillis
)

if (showDatePicker) {
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { viewModel.setBirthDate(it) }
                showDatePicker = false
            }) { Text("Xác nhận", color = HealthPrimary) }
        },
        dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
                Text("Hủy", color = HealthMuted)
            }
        }
    ) { DatePicker(state = datePickerState) }
}
    // Show vaccine review modal after saving a new pet
    if (uiState.showVaccineReview) {
        VaccineReviewModal(
            generatedRecords = uiState.pendingVaccineRecords,
            onConfirm = { selected ->
                viewModel.confirmVaccineReview(selected) {
                    onSaved(false)
                }
            },
            onClose = {
                viewModel.skipVaccineReview {
                    onSaved(false)
                }
            }
        )
    }

    LaunchedEffect(petId) {
    if (petId.isNullOrBlank() || petId.contains("{")) {
        viewModel.resetToNewPet()
    } else if (!uiState.isEditMode) {
        viewModel.loadPet(petId)
    }
}

//The `contains("{")` catches the unsubstituted template string as a safety net.

//**How vaccine dates are scheduled**

//The algorithm preserves relative spacing from the WSAVA `offsetDays`. Starting from tomorrow:
//DHPP #1  offsetDays=42  → today + 1 day        (the minimum, anchor point)
//DHPP #2  offsetDays=70  → today + 1 + 28 days  (gap: 70-42=28)
//DHPP #3  offsetDays=112 → today + 1 + 70 days  (gap: 112-42=70)
//Rabies   offsetDays=112 → today + 1 + 70 days  (same day as DHPP #3 ✓)

    var typeExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val types = listOf("Chó", "Mèo", "Chim", "Khác")
    val genders = listOf("Đực", "Cái")

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Đồng ý", color = HealthPrimary, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Lỗi", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.errorMessage ?: "") },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SimpleTopBar(
            title       = if (uiState.isEditMode) "Chỉnh sửa thông tin" else "Thêm thú cưng",
            onBackClick = onBackClick
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Selection Section
                val context = LocalContext.current
                val imagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri -> 
                    uri?.let {
                        val bytes = compressImageForAvatar(context, it)
                        if (bytes != null) {
                            viewModel.onImageSelected(bytes)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(HealthSurface)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.selectedImageBytes != null || uiState.existingImageUrl != null) {
                        AsyncImage(
                            model = uiState.selectedImageBytes ?: uiState.existingImageUrl,
                            contentDescription = "Pet Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            color = HealthPrimary,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = HealthPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Thêm ảnh thú cưng",
                                color = HealthPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                CustomTextField(
                    value = uiState.name,
                    onValueChange = viewModel::setName,
                    placeholder = "Tên thú cưng",
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
                        placeholder = { Text("Loại thú cưng", color = HealthMuted) },
                        trailingIcon = { 
                            Text("▼", color = HealthPrimary, fontSize = 12.sp) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                            unfocusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { typeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(Color.White).fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t, fontWeight = FontWeight.Medium) },
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
                    placeholder = "Giống (Poodle, Mèo Anh...)",
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
                        placeholder = { Text("Giới tính", color = HealthMuted) },
                        trailingIcon = { 
                            Text("▼", color = HealthPrimary, fontSize = 12.sp) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                            unfocusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { genderExpanded = true }
                    )
                    DropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                        modifier = Modifier.background(Color.White).fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        genders.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.setGender(g)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Age + weight
                // Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                //     CustomTextField(
                //         value = uiState.ageYears,
                //         onValueChange = viewModel::setAgeYears,
                //         modifier = Modifier.weight(1f),
                //         placeholder = "Năm tuổi",
                //         keyboardType = KeyboardType.Number,
                //         imeAction = ImeAction.Next,
                //         onImeAction = { focusManager.moveFocus(FocusDirection.Right) }
                //     )
                //     CustomTextField(
                //         value = uiState.ageMonths,
                //         onValueChange = viewModel::setAgeMonths,
                //         modifier = Modifier.weight(1f),
                //         placeholder = "Tháng tuổi",
                //         keyboardType = KeyboardType.Number,
                //         imeAction = ImeAction.Next,
                //         onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                //     )
                // }
                Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = uiState.birthDateMillis?.let {
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(it))
        } ?: "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        placeholder = { Text("Ngày sinh", color = HealthMuted) },
        trailingIcon = { Text("📅", fontSize = 16.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = HealthSurface.copy(alpha = 0.5f),
            unfocusedContainerColor = HealthSurface.copy(alpha = 0.5f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(16.dp)
    )
    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
}

                CustomTextField(
                    value = uiState.weightKg,
                    onValueChange = viewModel::setWeightKg,
                    placeholder = "Cân nặng (kg)",
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                CustomTextField(
                    value = uiState.note,
                    onValueChange = viewModel::setNote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = "Ghi chú thêm về thú cưng của bạn...",
                    singleLine = false,
                    maxLines = 5,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.save(onSuccess = { onSaved(uiState.isEditMode) }) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .height(58.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthPrimary,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        color       = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isEditMode) "Cập nhật thông tin" else "Lưu thông tin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}
