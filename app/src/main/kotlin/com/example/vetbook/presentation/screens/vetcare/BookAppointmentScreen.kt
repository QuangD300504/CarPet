package com.example.vetbook.presentation.screens.vetcare

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    onBackClick: () -> Unit = {}
) {
    val uiState by vetsViewModel.uiState.collectAsState()
    val doctor = uiState.veterinarians.find { it.id == doctorId }
    val bookingState by bookingViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDate by remember { mutableStateOf(2) }
    var selectedTime by remember { mutableStateOf(0) }

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookAppointmentViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                bookingViewModel.reset()
            }
            is BookAppointmentViewModel.UiState.PaymentReady -> {
                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(context, Uri.parse(state.checkoutUrl))
                bookingViewModel.reset()
            }
            else -> Unit
        }
    }

    if (doctor != null) {
        BookAppointmentContent(
            doctor = doctor,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            bookingState = bookingState,
            snackbarHostState = snackbarHostState,
            onDateSelect = { selectedDate = it },
            onTimeSelect = { selectedTime = it },
            onConfirmClick = {
                val appointmentAt = buildAppointmentDate(selectedDate, selectedTime)
                bookingViewModel.confirmAndPay(
                    veterinarianId = doctorId,
                    appointmentAt = appointmentAt,
                    totalPrice = MVP_FIXED_PRICE_VND,
                    durationMinutes = 30,
                    notes = null
                )
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFFFFEB3B))
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
                // Doctor image area (no internal back button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.pawns),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Yellow overlay card info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = doctor.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = doctor.specialty,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
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

                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    val dates = listOf(3, 4, 5, 6, 7, 8, 9)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        days.take(5).forEachIndexed { index, day ->
                            CalendarDayItem(
                                day = day,
                                date = dates[index],
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

                    val timeSlots = listOf("9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        timeSlots.forEachIndexed { index, time ->
                            TimeSlotItem(
                                time = time,
                                isSelected = selectedTime == index,
                                onClick = { onTimeSelect(index) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onConfirmClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = bookingState !is BookAppointmentViewModel.UiState.Loading
                    ) {
                        if (bookingState is BookAppointmentViewModel.UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
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
            color = if (isSelected) Color.White else Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isSelected) Color.Black else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black
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
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFF9800) else Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = time,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookAppointmentScreenPreview() {
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
        onDateSelect = {},
        onTimeSelect = {},
        onBackClick = {},
        onConfirmClick = {}
    )
}
