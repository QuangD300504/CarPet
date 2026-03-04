package com.example.vetbook.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.viewmodels.admin.AdminAppointmentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminAppointmentsScreen(
    viewModel: AdminAppointmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center), color = Brand
            )
            uiState.appointments.isEmpty() -> Text(
                "No appointments found.", color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.appointments) { appointment ->
                    AdminAppointmentItemCard(appointment)
                }
            }
        }
    }
}

@Composable
private fun AdminAppointmentItemCard(appointment: Appointment) {
    val formatter = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
    val dateStr = try {
        formatter.format(Date(appointment.appointmentAt.toEpochMilli()))
    } catch (e: Exception) { "Unknown date" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = dateStr, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                StatusChip(appointment.status)
            }
            Text("Vet ID: ${appointment.veterinarianId}", fontSize = 12.sp, color = Color.Gray)
            Text("User: ${appointment.userId}", fontSize = 12.sp, color = Color.Gray)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${appointment.durationMinutes} min", fontSize = 12.sp, color = Color.Gray)
                Text(
                    "Payment: ${appointment.paymentStatus}",
                    fontSize = 12.sp,
                    color = if (appointment.paymentStatus == "paid") Color(0xFF22C55E) else Color.Gray
                )
            }
        }
    }
}


@Composable
private fun StatusChip(status: String) {
    val bg = when (status.lowercase()) {
        "confirmed", "paid" -> Color(0xFFDCFCE7)
        "cancelled" -> Color(0xFFFEE2E2)
        "pending" -> Color(0xFFFEF9C3)
        else -> Color(0xFFF1F5F9)
    }
    val fg = when (status.lowercase()) {
        "confirmed", "paid" -> Color(0xFF16A34A)
        "cancelled" -> Color(0xFFDC2626)
        "pending" -> Color(0xFFCA8A04)
        else -> Color.Gray
    }
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
