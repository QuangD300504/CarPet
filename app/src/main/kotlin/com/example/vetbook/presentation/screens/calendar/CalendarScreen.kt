@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.vetbook.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.viewmodels.CalendarUiState
import com.example.vetbook.presentation.viewmodels.CalendarViewModel
import com.example.vetbook.presentation.components.calendar.RateDoctorDialog
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    veterinariansViewModel: com.example.vetbook.presentation.viewmodels.VeterinariansViewModel = hiltViewModel(),
    onSubmitReview: (appointmentId: String, doctorId: String, rating: Int, comment: String?) -> Unit = { _, _, _, _ -> },
    onContinuePayment: (checkoutUrl: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewMessage by veterinariansViewModel.reviewMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // VAC-06: Refresh appointments when returning to Calendar tab so newly
    // booked vaccine-linked appointments appear without a full app restart.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.paymentUrl) {
        uiState.paymentUrl?.let { url ->
            viewModel.clearPaymentUrl()
            onContinuePayment(url)
        }
    }

    LaunchedEffect(reviewMessage) {
        reviewMessage?.let {
            snackbarHostState.showSnackbar(it)
            veterinariansViewModel.clearReviewMessage()
        }
    }

    CalendarContent(
        uiState = uiState,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onDateSelected = viewModel::onDateSelected,
        getAppointmentsForDate = viewModel::getAppointmentsForDate,
        onSubmitReview = onSubmitReview,
        onContinuePayment = { apptId -> viewModel.retryPayment(apptId) },
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    getAppointmentsForDate: (LocalDate) -> List<Appointment>,
    onSubmitReview: (appointmentId: String, doctorId: String, rating: Int, comment: String?) -> Unit,
    onContinuePayment: (appointmentId: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showReminder by remember { mutableStateOf(false) }
    var showAppointmentDetail by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showRateDialog by remember { mutableStateOf(false) }
    var rateTargetAppointment by remember { mutableStateOf<Appointment?>(null) }
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { scaffoldPadding ->
        Box(modifier = Modifier.padding(scaffoldPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Lịch Hẹn",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Quản lý lịch khám thú cưng",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Brand.copy(alpha = 0.1f),
                            onClick = { showReminder = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Reminders",
                                    tint = Brand,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 0.5.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onPreviousMonth,
                                    modifier = Modifier.clip(CircleShape).background(Color(0xFFF3F4F6))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev", modifier = Modifier.size(18.dp))
                                }

                                Text(
                                    text = uiState.currentMonth.format(formatter),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )

                                IconButton(
                                    onClick = onNextMonth,
                                    modifier = Modifier.clip(CircleShape).background(Color(0xFFF3F4F6))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next", modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            CalendarMonthGrid(
                                currentMonth = uiState.currentMonth,
                                selectedDate = uiState.selectedDate,
                                appointments = uiState.appointments,
                                onSelectDay = onDateSelected
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomEnd = 0.dp, bottomStart = 0.dp))
                            .background(Color.White)
                            .padding(top = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )

                            val dailyAppointments = getAppointmentsForDate(uiState.selectedDate)
                            Surface(
                                shape = CircleShape,
                                color = Brand.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${dailyAppointments.size} Lịch hẹn",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brand
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val dailyAppointments = getAppointmentsForDate(uiState.selectedDate)
                        if (dailyAppointments.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFFE5E7EB)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Không có lịch hẹn cho ngày này",
                                        color = Color.Gray,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                dailyAppointments.forEach { appointment ->
                                    ScheduleStickyNote(
                                        appointment = appointment,
                                        onMoreClick = {
                                            selectedAppointment = appointment
                                            showAppointmentDetail = true
                                        }
                                    )
                                }
                            }
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
                    ReminderSheetContent(onClose = { showReminder = false })
                }
            }

            if (showAppointmentDetail && selectedAppointment != null) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showAppointmentDetail = false },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    AppointmentDetailSheet(
                        appointment = selectedAppointment!!,
                        onClose = { showAppointmentDetail = false },
                        onContinuePayment = { apptId -> onContinuePayment(apptId) },
                        onRateDoctor = { appointment ->
                            rateTargetAppointment = appointment
                            showRateDialog = true
                            showAppointmentDetail = false
                        }
                    )
                }
            }

            if (showRateDialog && rateTargetAppointment != null) {
                RateDoctorDialog(
                    doctorName = rateTargetAppointment!!.veterinarianName.ifEmpty { "Bác sĩ" },
                    onDismiss = {
                        showRateDialog = false
                        rateTargetAppointment = null
                    },
                    onSubmit = { rating, comment ->
                        val appt = rateTargetAppointment!!
                        onSubmitReview(appt.id, appt.veterinarianId, rating, comment)
                        showRateDialog = false
                        rateTargetAppointment = null
                    }
                )
            }
        }
    }
}

@Composable
private fun ScheduleStickyNote(
    appointment: Appointment,
    onMoreClick: () -> Unit
) {
    val time = appointment.appointmentAt.atZone(java.time.ZoneId.systemDefault()).toLocalTime()

    // FIX: past PENDING_PAYMENT appointments show as OVERDUE instead of yellow
    val isPast = remember(appointment.appointmentAt) {
        appointment.appointmentAt.isBefore(Instant.now())
    }
    val isOverdue = appointment.status.uppercase() == "PENDING_PAYMENT" && isPast

    val colors = listOf(
        Color(0xFFE0F2FE) to Color(0xFF0369A1),
        Color(0xFFDCFCE7) to Color(0xFF15803D),
        Color(0xFFFEF9C3) to Color(0xFFA16207),
        Color(0xFFFEE2E2) to Color(0xFFB91C1C),
        Color(0xFFF3E8FF) to Color(0xFF7E22CE)
    )
    val (bgColor, textColor) = if (isOverdue) {
        Color(0xFFFEE2E2) to Color(0xFFB91C1C)
    } else {
        colors[appointment.id.hashCode().coerceAtLeast(0) % colors.size]
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("a")),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.veterinarianName.ifEmpty { "Khám định kỳ" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = textColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.padding(4.dp).size(12.dp),
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thú cưng: ${appointment.petNames.firstOrNull() ?: "Chưa rõ"}",
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                // Show OVERDUE label on the card itself
                if (isOverdue) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quá hạn thanh toán",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun AppointmentDetailSheet(
    appointment: Appointment,
    onClose: () -> Unit,
    onContinuePayment: ((String) -> Unit)? = null,
    onRateDoctor: ((Appointment) -> Unit)? = null
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }
    val zonedDateTime = remember(appointment.appointmentAt) {
        appointment.appointmentAt.atZone(java.time.ZoneId.systemDefault())
    }

    // FIX: treat past PENDING_PAYMENT appointments as OVERDUE so the payment
    // button is hidden and the status chip reflects the correct state.
    val isPast = remember(appointment.appointmentAt) {
        appointment.appointmentAt.isBefore(Instant.now())
    }
    val normalizedStatus = when {
        appointment.status.uppercase() == "PENDING_PAYMENT" && isPast -> "OVERDUE"
        else -> appointment.status.uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chi tiết lịch hẹn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClose) {
                Text("Đóng")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = appointment.veterinarianName.ifEmpty { "Bác sĩ thú y" },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        if (appointment.clinicName.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appointment.clinicName,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        if (appointment.clinicAddress.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Brand,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = appointment.clinicAddress,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = Brand,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = zonedDateTime.toLocalDate().format(dateFormatter),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${zonedDateTime.toLocalTime().format(timeFormatter)} • ${appointment.durationMinutes} phút",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        if (appointment.petNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Brand,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Thú cưng: ${appointment.petNames.joinToString()}",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (statusBg, statusFg) = when (normalizedStatus) {
                "COMPLETED"             -> Color(0xFFE8F5E9) to Color(0xFF22C55E)
                "CANCELLED"             -> Color(0xFFFFEBEE) to Color(0xFFEF4444)
                "UPCOMING", "CONFIRMED" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
                "PENDING_PAYMENT"       -> Color(0xFFFFF8E1) to Color(0xFFF59E0B)
                "OVERDUE"               -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
                else                    -> Color(0xFFE5E7EB) to Color(0xFF374151)
            }

            StatusChip(
                label = if (normalizedStatus == "OVERDUE") "OVERDUE" else appointment.status,
                background = statusBg,
                foreground = statusFg
            )

            StatusChip(
                label = appointment.paymentStatus,
                background = Color(0xFFDCFCE7),
                foreground = Color(0xFF15803D)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Giá: ${"%,.0f".format(appointment.totalPrice).replace(",", ".")}đ",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        // FIX: only show "Tiếp tục thanh toán" if the appointment is NOT in the past.
        // Past PENDING_PAYMENT appointments are treated as OVERDUE and the button is hidden.
        if (normalizedStatus == "PENDING_PAYMENT" && onContinuePayment != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onContinuePayment.invoke(appointment.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tiếp tục thanh toán", fontWeight = FontWeight.Bold)
            }
        }

        if (!appointment.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ghi chú",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appointment.notes.orEmpty(),
                fontSize = 13.sp,
                color = Color(0xFF4B5563)
            )
        }

        if (onRateDoctor != null && appointment.status.uppercase() == "COMPLETED") {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { onRateDoctor.invoke(appointment) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Đánh giá bác sĩ", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    background: Color,
    foreground: Color
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = foreground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CalendarMonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    onSelectDay: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value - 1

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            daysOfWeek.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray.copy(alpha = 0.4f)
                )
            }
        }

        var currentDayCounter = 1
        for (i in 0..5) {
            if (currentDayCounter > daysInMonth) break
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (j in 0..6) {
                    val dayOfMonth = if (i == 0 && j < firstDayOfWeek || currentDayCounter > daysInMonth) {
                        null
                    } else {
                        currentDayCounter++
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayOfMonth != null) {
                            val date = currentMonth.atDay(dayOfMonth)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val hasAppointments = appointments.any { appt ->
                                appt.appointmentAt
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate() == date
                            }

                            val contentColor = when {
                                isSelected -> Color.White
                                isToday -> Brand
                                else -> Color(0xFF1F2937)
                            }

                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Brand else Color.Transparent,
                                onClick = { onSelectDay(date) }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = contentColor
                                    )
                                    if (hasAppointments) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .background(
                                                    if (isSelected) Color.White else Brand,
                                                    CircleShape
                                                )
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
    val yellow = Brand

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