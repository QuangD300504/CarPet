package com.example.vetbook.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.presentation.viewmodels.CalendarUiState
import com.example.vetbook.presentation.viewmodels.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CalendarContent(
        uiState = uiState,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onSelectDay = viewModel::onDateSelected,
        getAppointmentsForDate = viewModel::getAppointmentsForDate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    getAppointmentsForDate: (LocalDate) -> List<Appointment>
) {
    val yellow = Color(0xFFFFEB3B)
    var showReminder by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = Color(0xFF6F6F6F),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + uiState.currentMonth.year,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = Color(0xFF6F6F6F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CalendarMonthGrid(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                appointments = uiState.appointments,
                onSelectDay = onSelectDay
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = uiState.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy")),
                    fontSize = 12.sp,
                    color = Color(0xFF6F6F6F),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                val selectedAppointments = getAppointmentsForDate(uiState.selectedDate)

                if (selectedAppointments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No appointments scheduled",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedAppointments) { appointment ->
                            AppointmentItem(appointment = appointment)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showReminder = true },
                        colors = ButtonDefaults.buttonColors(containerColor = yellow),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Text(
                            text = "Reminder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        if (showReminder) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showReminder = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ReminderSheetContent(
                    onClose = { showReminder = false }
                )
            }
        }
    }
}

@Composable
private fun AppointmentItem(appointment: Appointment) {
    val yellow = Color(0xFFFFEB3B)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Veterinary Care",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Appointment #${appointment.id.takeLast(4)}",
                    fontSize = 12.sp,
                    color = Color(0xFF6F6F6F)
                )
            }

            Surface(
                color = yellow,
                shape = RoundedCornerShape(4.dp)
            ) {
                val time = appointment.appointmentAt
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                
                Text(
                    text = time,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    onSelectDay: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = Color(0xFF6F6F6F)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        var currentDay = 1
        for (i in 0..5) { // Max 6 weeks
            if (currentDay > daysInMonth) break
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (j in 0..6) {
                    val dayOfMonth = if (i == 0 && j < firstDayOfWeek || currentDay > daysInMonth) {
                        null
                    } else {
                        currentDay++
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayOfMonth != null) {
                            val date = currentMonth.atDay(dayOfMonth)
                            val isSelected = date == selectedDate
                            val hasAppointments = appointments.any { appt ->
                                appt.appointmentAt
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate() == date
                            }

                            val bg = if (isSelected) Color(0xFFB8F06A) else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(bg, RoundedCornerShape(12.dp))
                                    .clickable { onSelectDay(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    if (hasAppointments && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(Color.Red, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderSheetContent(
    onClose: () -> Unit
) {
    val dividerColor = Color(0xFFEAEAEA)
    val yellow = Color(0xFFFFEB3B)

    var serviceExpanded by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf("Service") }

    var dateExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("Tomorrow") }

    var timeExpanded by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("Set time...") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 18.dp)
    ) {
        // Title
        Text(
            text = "Reminder",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            textAlign = TextAlign.Center
        )

        // Service row
        ReminderRow(
            label = "Service",
            value = selectedService,
            onClick = { serviceExpanded = true }
        )
        DividerLine(dividerColor)
        DropdownMenu(
            expanded = serviceExpanded,
            onDismissRequest = { serviceExpanded = false }
        ) {
            listOf("Pamper", "Vet care", "Stay & Care").forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        selectedService = it
                        serviceExpanded = false
                    }
                )
            }
        }

        // Date row
        ReminderRow(
            label = "Date",
            value = selectedDate,
            onClick = { dateExpanded = true }
        )
        DividerLine(dividerColor)
        DropdownMenu(
            expanded = dateExpanded,
            onDismissRequest = { dateExpanded = false }
        ) {
            listOf("Today", "Tomorrow", "Next week").forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        selectedDate = it
                        dateExpanded = false
                    }
                )
            }
        }

        // Time row
        ReminderRow(
            label = "Time",
            value = selectedTime,
            onClick = { timeExpanded = true }
        )
        DividerLine(dividerColor)
        DropdownMenu(
            expanded = timeExpanded,
            onDismissRequest = { timeExpanded = false }
        ) {
            listOf("9:00 AM", "10:00 AM", "2:00 PM", "4:00 PM").forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        selectedTime = it
                        timeExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onClose() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "X",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Button(
                onClick = { onClose() },
                colors = ButtonDefaults.buttonColors(containerColor = yellow),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 18.dp,
                    vertical = 10.dp
                )
            ) {
                Text(
                    text = "Reminder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ReminderRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 12.sp,
            color = if (value == "Service" || value == "Set time...") Color(0xFF2F5BFF) else Color.Black,
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFF6F6F6F),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DividerLine(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    CalendarScreen()
}
