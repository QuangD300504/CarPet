package com.example.vetbook.presentation.screens.vetcare

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.ArrowDropDown
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.utils.PayosLauncher
import com.example.vetbook.utils.DeepLinkHandler
import com.example.vetbook.presentation.viewmodels.BookAppointmentViewModel
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel
import com.example.vetbook.presentation.components.calendar.SlotGrid
import com.example.vetbook.presentation.components.calendar.SlotOption
import com.example.vetbook.presentation.components.common.SnackbarType
import com.example.vetbook.presentation.components.common.VetBookSnackbar
import com.example.vetbook.presentation.components.common.VetBookSnackbarHost
import com.example.vetbook.R
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import java.time.LocalDateTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    doctorId: String,
    vetsViewModel: VeterinariansViewModel = hiltViewModel(),
    bookingViewModel: BookAppointmentViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onShowPayment: (checkoutUrl: String) -> Unit = {},
    onPaymentFinished: (isSuccess: Boolean, appointmentId: String, vetName: String?, appointmentAt: java.util.Date?) -> Unit = { _, _, _, _ -> }
) {
    val uiState by vetsViewModel.uiState.collectAsState()
    val doctor = uiState.veterinarians.find { it.id == doctorId }
    val bookingState by bookingViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val pets by bookingViewModel.pets.collectAsState()
    val selectedPetIds by bookingViewModel.selectedPetIds.collectAsState()
    val bookedSlots by bookingViewModel.bookedSlots.collectAsState()
    val timeSlots = bookingViewModel.timeSlots

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Compute past slots — only when selected date is today, slots before current time are disabled
    val pastSlots = remember(selectedDateMillis) {
        val today = LocalDate.now()
        val selectedDate = selectedDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        if (selectedDate != today) {
            emptySet()
        } else {
            val now = LocalTime.now()
            SlotOption.defaults
                .filter { it.time.isBefore(now) || it.time.equals(now) }
                .map { it.label }
                .toSet()
        }
    }

    // Reload locked slots whenever the doctor or selected date changes
    LaunchedEffect(doctorId, selectedDateMillis) {
        selectedDateMillis?.let { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            bookingViewModel.loadBookedSlots(doctorId, date.year, date.monthValue, date.dayOfMonth)
        }
    }

    var isResultHandled by remember { mutableStateOf(false) }

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookAppointmentViewModel.UiState.Error -> {
                scope.launch { VetBookSnackbar.show(snackbarHostState, state.message, SnackbarType.Error) }
                bookingViewModel.reset()
            }
            is BookAppointmentViewModel.UiState.PaymentReady -> {
                isResultHandled = false
                onShowPayment(state.checkoutUrl)
                bookingViewModel.reset()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        DeepLinkHandler.paymentResult.collect { isSuccess ->
            if (isResultHandled) return@collect
            
            val apptId = bookingViewModel.pendingAppointmentId
            val lockId = bookingViewModel.pendingLockId
            if (apptId != null && lockId != null) {
                isResultHandled = true
                if (isSuccess) {
                    bookingViewModel.onPaymentSuccess(apptId)
                } else {
                    bookingViewModel.cancelAppointment(apptId, lockId)
                }
                DeepLinkHandler.clear()
                onPaymentFinished(
    isSuccess,
    bookingViewModel.pendingAppointmentId ?: "",
    bookingViewModel.pendingVetName,
    bookingViewModel.pendingAppointmentAt
)
                
                selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    bookingViewModel.loadBookedSlots(doctorId, date.year, date.monthValue, date.dayOfMonth)
                }
            }
        }
    }

    if (doctor != null) {
        BookAppointmentContent(
            doctor            = doctor,
            pets              = pets,
            selectedPetIds    = selectedPetIds,
            selectedDateMillis = selectedDateMillis,
            selectedSlot      = selectedSlot,
            noteText          = noteText,
            bookingState      = bookingState,
            snackbarHostState = snackbarHostState,
            bookedSlots       = bookedSlots,
            pastSlots         = pastSlots,
            timeSlots         = timeSlots,
            onBackClick       = onBackClick,
            onPetToggle       = { bookingViewModel.togglePetSelection(it) },
            onDateSelect      = { selectedDateMillis = it },
            onSlotSelect      = { selectedSlot = it },
            onNoteChange      = { noteText = it },
            onConfirmClick    = {
                val date = selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                val slotLabel = selectedSlot
                if (date != null && slotLabel != null) {
                    val time = SlotOption.defaults.find { it.label == slotLabel }?.time
                        ?: LocalTime.parse(slotLabel)
                    val appointmentAt = Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant())
                    val selectedPetName = pets.firstOrNull { it.id in selectedPetIds }?.name ?: "Thú cưng"
bookingViewModel.confirmAndPay(
    veterinarianId  = doctorId,
    appointmentAt   = appointmentAt,
    totalPrice      = doctor.servicePrice * selectedPetIds.size,
    durationMinutes = 30,
    notes           = if (noteText.isBlank()) null else noteText,
    vetName         = doctor.name,
    petName         = selectedPetName
)
                }
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(HealthSurface), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = HealthPrimary)
            } else {
                Text(text = "Không tìm thấy bác sĩ", color = HealthMuted)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookAppointmentContent(
    doctor: Veterinarian,
    pets: List<com.example.vetbook.domain.models.Pet>,
    selectedPetIds: Set<String>,
    selectedDateMillis: Long?,
    selectedSlot: String?,
    noteText: String,
    bookingState: BookAppointmentViewModel.UiState,
    snackbarHostState: SnackbarHostState,
    bookedSlots: Set<String> = emptySet(),
    pastSlots: Set<String> = emptySet(),
    timeSlots: List<String> = emptyList(),
    onBackClick: () -> Unit = {},
    onPetToggle: (String) -> Unit,
    onDateSelect: (Long?) -> Unit,
    onSlotSelect: (String?) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirmClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                return utcTimeMillis >= today
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelect(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { VetBookSnackbarHost(snackbarHostState) },
        containerColor = HealthSurface,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tổng thanh toán",
                            style = MaterialTheme.typography.labelMedium,
                            color = HealthMuted
                        )
                        val total = doctor.servicePrice * selectedPetIds.size
                        Text(
                            text = String.format("%,.0f đ", total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = HealthPrimary
                        )
                    }
                    Button(
                        onClick = onConfirmClick,
                        enabled = selectedDateMillis != null && selectedSlot != null && selectedPetIds.isNotEmpty() && bookingState !is BookAppointmentViewModel.UiState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HealthPrimary,
                            disabledContainerColor = HealthPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.width(180.dp)
                    ) {
                        if (bookingState is BookAppointmentViewModel.UiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Đặt Lịch & Thanh Toán")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.pawns),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.White.copy(0.9f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthPrimary)
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(doctor.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(doctor.specialty, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.9f))
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Section: Pets
                SectionTitle(title = "Chọn Thú Cưng", icon = Icons.Default.Pets)
                Spacer(modifier = Modifier.height(16.dp))
                if (pets.isEmpty()) {
                    Text("Vui lòng thêm thú cưng trong hồ sơ của bạn để đặt lịch khám", color = HealthMuted, style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pets) { pet ->
                            PetSelectionItem(
                                pet = pet,
                                isSelected = selectedPetIds.contains(pet.id),
                                onClick = { onPetToggle(pet.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section: Date
                SectionTitle(title = "Chọn Ngày Khám", icon = Icons.Default.Event)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthPrimary)
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val dateText = selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale("vi")))
                    } ?: "Chọn ngày dự kiến"
                    Text(dateText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section: Time
                SectionTitle(title = "Chọn Giờ Khám", icon = Icons.Default.Schedule)
                Spacer(modifier = Modifier.height(12.dp))
                if (selectedDateMillis == null) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthPrimary),
                        enabled = false
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vui lòng chọn ngày trước", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    SlotGrid(
                        slots = SlotOption.defaults,
                        bookedSlots = bookedSlots,
                        pastSlots = pastSlots,
                        selectedSlot = selectedSlot,
                        onSlotSelected = { onSlotSelect(it) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section: Notes
                SectionTitle(title = "Lưu Ý Cho Bác Sĩ", icon = Icons.Default.Description)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Mô tả sơ qua tình trạng thú cưng của bạn...", color = HealthMuted, fontSize = 14.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthPrimary,
                        unfocusedBorderColor = HealthMuted.copy(alpha = 0.2f),
                        cursorColor = HealthPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = HealthPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PetSelectionItem(
    pet: com.example.vetbook.domain.models.Pet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) HealthPrimary else Color.White,
        border = if (!isSelected) BorderStroke(1.dp, HealthMuted.copy(alpha = 0.2f)) else null,
        shadowElevation = if (isSelected) 6.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.2f) else HealthSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) Color.White else HealthPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pet.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black,
                maxLines = 1
            )
            Text(
                text = pet.type,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else HealthMuted,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookAppointmentScreenPreview() {
    BookAppointmentContent(
        doctor = Veterinarian(
            id = "1",
            name = "BS. Nguyễn Văn A",
            specialty = "Chuyên gia tim mạch",
            experience = "5 năm",
            rating = 4.9,
            reviewsCount = 95,
            initials = "NA",
            bio = "",
            servicePrice = 100000.0
        ),
        pets = listOf(
            com.example.vetbook.domain.models.Pet(id = "1", name = "Lu Lu", type = "Chó", breed = "Poodle")
        ),
        selectedPetIds = setOf("1"),
        selectedDateMillis = null,
        selectedSlot = null,
        noteText = "",
        bookingState = BookAppointmentViewModel.UiState.Idle,
        snackbarHostState = remember { SnackbarHostState() },
        bookedSlots = emptySet(),
        timeSlots = listOf("09:00", "09:30", "10:00"),
        onPetToggle = {},
        onDateSelect = {},
        onSlotSelect = {},
        onNoteChange = {},
        onConfirmClick = {}
    )
}
