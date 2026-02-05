package com.example.vetbook.presentation.screens.calendar

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBackClick: () -> Unit = {}
) {
    val yellow = Color(0xFFFFEB3B)
    val lightYellow = Color(0xFFFFF3C4)

    var selectedDay by remember { mutableStateOf(4) }
    var showHistory by remember { mutableStateOf(false) }
    var showReminder by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top yellow area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(yellow)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top bar row
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        text = "Calendar",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    // Spacer to balance center title
                    Box(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calendar card container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = lightYellow,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Month header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Prev",
                                tint = Color(0xFF6F6F6F),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "January",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                tint = Color(0xFF6F6F6F),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month grid (UI-only)
                        CalendarMonthGrid(
                            selectedDay = selectedDay,
                            onSelectDay = { selectedDay = it },
                            onLongPressDay = { showHistory = true }
                        )
                    }
                }
            }

            // Bottom white section (day detail)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Monday, January 20 2026",
                    fontSize = 12.sp,
                    color = Color(0xFF6F6F6F),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Floating reminder button area (matches screenshot position roughly)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showReminder = true },
                        colors = ButtonDefaults.buttonColors(containerColor = yellow),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
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

        if (showHistory) {
            CalendarHistoryCard(
                onDismiss = { showHistory = false },
                onGo = {
                    showHistory = false
                }
            )
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
private fun CalendarMonthGrid(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onLongPressDay: (Int) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

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

        // Simple 5-week grid, UI-only
        val weeks = listOf(
            listOf(null, null, null, null, 1, 2, 3),
            listOf(4, 5, 6, 7, 8, 9, 10),
            listOf(11, 12, 13, 14, 15, 16, 17),
            listOf(18, 19, 20, 21, 22, 23, 24),
            listOf(25, 26, 27, 28, 29, 30, 31)
        )

        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val isSelected = day == selectedDay
                            val bg = when {
                                isSelected -> Color(0xFFB8F06A) // green highlight from screenshot
                                day == 10 -> Color(0xFFFF8A80) // red-ish dot day (UI only)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(bg, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectDay(day)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHistoryCard(
    onDismiss: () -> Unit,
    onGo: () -> Unit
) {
    // Center overlay card (UI-only)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.15f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Tuesday, January 4 2026",
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pamper",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MeoMeo Beauty",
                            fontSize = 12.sp,
                            color = Color(0xFF6F6F6F)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFEB3B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "9AM",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.size(10.dp))

                        Surface(
                            modifier = Modifier.clickable { onGo() },
                            color = Color(0xFFFFEB3B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Go",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
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
            imageVector = Icons.Default.ArrowForward,
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
