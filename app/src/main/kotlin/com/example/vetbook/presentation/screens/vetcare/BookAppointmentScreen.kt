package com.example.vetbook.presentation.screens.vetcare

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.R
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.BookAppointmentViewModel
import com.example.vetbook.presentation.viewmodels.VeterinariansViewModel
import java.util.Calendar
import java.util.Date

private const val MVP_FIXED_PRICE_VND = 100000.0

@Composable
fun BookAppointmentScreen(
    doctorId: String,
    vetsViewModel: VeterinariansViewModel = hiltViewModel(),
    bookingViewModel: BookAppointmentViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onPaymentReady: (checkoutUrl: String) -> Unit = {}
) {
    val uiState by vetsViewModel.uiState.collectAsState()
    val doctor = uiState.veterinarians.find { it.id == doctorId }
    val bookingState by bookingViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDate by remember { mutableStateOf(2) }
    var selectedTime by remember { mutableStateOf(0) }

    val context = LocalContext.current

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookAppointmentViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                bookingViewModel.reset()
            }
            is BookAppointmentViewModel.UiState.PaymentReady -> {
                try {
                    val intent = android.content.Intent(context, com.vnpay.authentication.VNP_AuthenticationActivity::class.java)
                    intent.putExtra("url", state.checkoutUrl)
                    intent.putExtra("tmn_code", "W8JDF86Z")
                    intent.putExtra("scheme", "vetbook-vnpay")
                    intent.putExtra("is_sandbox", true)

                    com.vnpay.authentication.VNP_AuthenticationActivity.setSdkCompletedCallback(object : com.vnpay.authentication.VNP_SdkCompletedCallback {
                        override fun sdkAction(action: String) {
                            android.util.Log.d("VNPAY", "Action: $action")
                            val isSuccess = action == "SuccessBackAction"
                            if (!isSuccess) {
                                bookingViewModel.cancelAppointment(state.appointmentId, state.lockId)
                            } else {
                                bookingViewModel.clearPendingUnlock()
                            }
                            onPaymentReady(isSuccess.toString()) // Passing "true" or "false" string to handle generic result
                        }
                    })
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("VNPAY", "Launch failed: ${e.message}")
                    bookingViewModel.cancelAppointment(state.appointmentId, state.lockId)
                }
                bookingViewModel.reset()
            }
            else -> Unit
        }
    }

    if (doctor != null) {
        BookAppointmentContent(
            doctor            = doctor,
            selectedDate      = selectedDate,
            selectedTime      = selectedTime,
            bookingState      = bookingState,
            snackbarHostState = snackbarHostState,
            onBackClick       = onBackClick,
            onDateSelect      = { selectedDate = it },
            onTimeSelect      = { selectedTime = it },
            onConfirmClick    = {
                val appointmentAt = buildAppointmentDate(selectedDate, selectedTime)
                bookingViewModel.confirmAndPay(
                    veterinarianId  = doctorId,
                    appointmentAt   = appointmentAt,
                    totalPrice      = MVP_FIXED_PRICE_VND,
                    durationMinutes = 30,
                    notes           = null
                )
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Brand)
            } else {
                Text(text = "Doctor not found")
            }
        }
    }
}

@Composable
private fun BookAppointmentContent(
    doctor: Veterinarian,
    selectedDate: Int,
    selectedTime: Int,
    bookingState: BookAppointmentViewModel.UiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit = {},
    onDateSelect: (Int) -> Unit,
    onTimeSelect: (Int) -> Unit,
    onConfirmClick: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Doctor hero image with floating back button overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    Image(
                        painter            = painterResource(R.drawable.pawns),
                        contentDescription = null,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                    // Floating back button — Type C HeroHeader
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 16.dp, top = 12.dp)
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint               = Brand
                            )
                        }
                    }
                    // Info card overlay
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Brand)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text      = doctor.name,
                                style     = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text  = doctor.specialty,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Appointment",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val calendar = Calendar.getInstance()
                    val daysList = mutableListOf<String>()
                    val datesList = mutableListOf<Int>()
                    
                    for (i in 0 until 14) {
                        val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                        daysList.add(dayFormat.format(calendar.time))
                        datesList.add(calendar.get(Calendar.DAY_OF_MONTH))
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(daysList.size) { index ->
                            CalendarDayItem(
                                day = daysList[index],
                                date = datesList[index],
                                isSelected = selectedDate == index,
                                onClick = { onDateSelect(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Available Time",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val timeSlots = listOf("9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM")

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(timeSlots.size) { index ->
                            TimeSlotItem(
                                time = timeSlots[index],
                                isSelected = selectedTime == index,
                                onClick = { onTimeSelect(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick  = onConfirmClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Brand,
                            contentColor   = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape   = RoundedCornerShape(14.dp),
                        enabled = bookingState !is BookAppointmentViewModel.UiState.Loading
                    ) {
                        if (bookingState is BookAppointmentViewModel.UiState.Loading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(24.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = "Confirm Appointment",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: String,
    date: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            color = if (isSelected) Color.Black else Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isSelected) Brand else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = date.toString(),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TimeSlotItem(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick  = onClick,
        modifier = modifier.height(48.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Brand else MaterialTheme.colorScheme.surfaceVariant,
            contentColor   = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text       = time,
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Build a Date from selected day-of-week index (0=Sun) and time slot index. */
private fun buildAppointmentDate(daysFromToday: Int, timeIndex: Int): java.util.Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysFromToday)
    val hourMinute = listOf(
        9 to 0, 9 to 30, 10 to 0, 10 to 30, 11 to 0,
        14 to 0, 14 to 30, 15 to 0, 15 to 30
    )
    val (hour, minute) = hourMinute.getOrElse(timeIndex) { 9 to 0 }
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

@Preview(showBackground = true)
@Composable
fun BookAppointmentScreenPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    BookAppointmentContent(
        doctor = Veterinarian(
            id = "1",
            name = "Dr. Ali Uzair",
            specialty = "Senior Cardiologist and Surgeon",
            experience = "3+ years",
            rating = "4.9",
            reviewsCount = 95,
            initials = "DAU",
            bio = ""
        ),
        selectedDate = 2,
        selectedTime = 0,
        bookingState = BookAppointmentViewModel.UiState.Idle,
        snackbarHostState = snackbarHostState,
        onDateSelect = {},
        onTimeSelect = {},
        onConfirmClick = {}
    )
}
