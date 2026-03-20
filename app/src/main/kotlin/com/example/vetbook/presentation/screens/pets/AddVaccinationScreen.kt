package com.example.vetbook.presentation.screens.pets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.theme.Background
import com.example.vetbook.presentation.viewmodels.AddVaccinationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccinationScreen(
    petId: String,
    petName: String = "",
    viewModel: AddVaccinationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAdditionalFields by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showVetDropdown by remember { mutableStateOf(false) }

    val types = listOf(
        VaccinationType.CORE to "Cốt lõi (Core)",
        VaccinationType.NON_CORE to "Khuyến nghị (Non-Core)",
        VaccinationType.OPTIONAL to "Tùy chọn (Optional)"
    )

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.scheduledDate?.toEpochMilli()
                ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.setScheduledDate(Instant.ofEpochMilli(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Xác nhận", color = HealthPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    // Error dialog
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

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBackClick()
                }) {
                    Text("Hủy bỏ", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Tiếp tục chỉnh sửa", color = HealthPrimary)
                }
            },
            title = { Text("Hủy thêm tiêm chủng?", fontWeight = FontWeight.Bold) },
            text = { Text("Dữ liệu bạn đã nhập sẽ không được lưu.") },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text("Thêm lịch tiêm chủng", fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (uiState.title.isNotBlank() || uiState.scheduledDate != null) {
                        showDiscardDialog = true
                    } else {
                        onBackClick()
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                // ===== REQUIRED FIELDS =====

                // Vaccine name
                CustomTextField(
                    value = uiState.title,
                    onValueChange = viewModel::setTitle,
                    placeholder = "Tên vaccine *",
                    singleLine = true
                )

                // Vaccine type dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = types.find { it.first == uiState.type }?.second ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Loại vaccine *", color = HealthMuted) },
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
                        modifier = Modifier.background(Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        types.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.setType(type)
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Scheduled date (invisible clickable over the styled field)
                Box(modifier = Modifier.fillMaxWidth()) {
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val dateText = uiState.scheduledDate
                        ?.atZone(ZoneId.systemDefault())
                        ?.format(dateFormatter)
                        ?: ""

                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Ngày tiêm *", color = HealthMuted) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Chọn ngày",
                                tint = HealthPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                            unfocusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = HealthPrimary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // ===== COLLAPSIBLE ADDITIONAL FIELDS =====

                TextButton(
                    onClick = { showAdditionalFields = !showAdditionalFields },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Thông tin bổ sung",
                        color = HealthPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (showAdditionalFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = HealthPrimary
                    )
                }

                AnimatedVisibility(visible = showAdditionalFields) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HorizontalDivider(color = Color(0xFFEEEFF2))

                        // Veterinarian autocomplete
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.veterinarianName,
                                onValueChange = { viewModel.setVeterinarianName(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Bác sĩ thực hiện", color = HealthMuted) },
                                trailingIcon = {
                                    if (uiState.veterinarianName.isNotBlank()) {
                                        IconButton(onClick = { viewModel.setVeterinarianName("") }) {
                                            Text("✕", color = HealthMuted, fontSize = 14.sp)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                                    unfocusedContainerColor = HealthSurface.copy(alpha = 0.5f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = HealthPrimary,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            DropdownMenu(
                                expanded = showVetDropdown && uiState.veterinarians.isNotEmpty(),
                                onDismissRequest = { showVetDropdown = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .fillMaxWidth(0.95f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                uiState.veterinarians.forEach { vet ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    vet.name,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    vet.specialty,
                                                    fontSize = 12.sp,
                                                    color = HealthMuted
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectVeterinarian(vet)
                                            showVetDropdown = false
                                        }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        if (uiState.veterinarians.isNotEmpty()) {
                                            showVetDropdown = !showVetDropdown
                                        }
                                    }
                            )
                        }

                        // Clinic name
                        CustomTextField(
                            value = uiState.clinicName,
                            onValueChange = viewModel::setClinicName,
                            placeholder = "Tên phòng khám",
                            singleLine = true
                        )

                        // Manufacturer
                        CustomTextField(
                            value = uiState.manufacturer,
                            onValueChange = viewModel::setManufacturer,
                            placeholder = "Hãng sản xuất",
                            singleLine = true
                        )

                        // Batch number
                        CustomTextField(
                            value = uiState.batchNumber,
                            onValueChange = viewModel::setBatchNumber,
                            placeholder = "Số lô",
                            singleLine = true
                        )

                        // Notes
                        CustomTextField(
                            value = uiState.notes,
                            onValueChange = viewModel::setNotes,
                            placeholder = "Ghi chú",
                            singleLine = false,
                            maxLines = 4,
                            modifier = Modifier.height(120.dp)
                        )

                        // Reminder toggle
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = HealthSurface.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Nhắc nhở trước",
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Switch(
                                        checked = uiState.reminderEnabled,
                                        onCheckedChange = viewModel::setReminderEnabled,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = HealthPrimary,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }

                                AnimatedVisibility(visible = uiState.reminderEnabled) {
                                    Column {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "${uiState.reminderDaysBefore} ngày trước",
                                            fontSize = 13.sp,
                                            color = HealthMuted
                                        )
                                        Slider(
                                            value = uiState.reminderDaysBefore.toFloat(),
                                            onValueChange = {
                                                viewModel.setReminderDaysBefore(it.toInt())
                                            },
                                            valueRange = 1f..30f,
                                            steps = 28,
                                            colors = SliderDefaults.colors(
                                                thumbColor = HealthPrimary,
                                                activeTrackColor = HealthPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Bottom Save button
            Button(
                onClick = { viewModel.save(onSuccess = onSaved) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .height(58.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        "Lưu tiêm chủng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}
