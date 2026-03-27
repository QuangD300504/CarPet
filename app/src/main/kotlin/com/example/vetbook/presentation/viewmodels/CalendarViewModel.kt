package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.domain.models.PaymentLink
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedWeekStart: LocalDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY),
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val paymentUrl: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var listenerJob: kotlinx.coroutines.Job? = null

    init { startListening() }

    fun refresh() {
        listenerJob?.cancel()
        startListening()
    }

    private fun startListening() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }
        listenerJob = viewModelScope.launch {
            bookingRepository.getUserAppointments(userId).collect { appointments ->
                val now = java.time.Instant.now()
                val pastUpcoming = appointments.filter {
                    it.status == "UPCOMING" && it.appointmentAt.isBefore(now)
                }
                pastUpcoming.forEach { appt -> launch { bookingRepository.markAppointmentCompleted(appt.id) } }
                _uiState.update { it.copy(appointments = appointments, isLoading = false) }
            }
        }
    }

    fun onPreviousMonth() {
        _uiState.update { 
            val newMonth = it.currentMonth.minusMonths(1)
            it.copy(currentMonth = newMonth)
        }
    }

    fun onNextMonth() {
        _uiState.update { 
            val newMonth = it.currentMonth.plusMonths(1)
            it.copy(currentMonth = newMonth)
        }
    }

    fun onPreviousWeek() {
        _uiState.update { it.copy(selectedWeekStart = it.selectedWeekStart.minusWeeks(1)) }
    }

    fun onNextWeek() {
        _uiState.update { it.copy(selectedWeekStart = it.selectedWeekStart.plusWeeks(1)) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * Helper to get appointments for a specific LocalDate
     */
    fun getAppointmentsForDate(date: LocalDate): List<Appointment> {
        return uiState.value.appointments.filter { appointment ->
            val apptDate = appointment.appointmentAt
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            apptDate == date
        }
    }
    
    /**
     * Helper to check if a date has any appointments (for markers)
     */
    fun hasAppointments(date: LocalDate): Boolean {
        return getAppointmentsForDate(date).isNotEmpty()
    }

    /**
     * Retry payment for a PENDING_PAYMENT appointment.
     * Generates a new PayOS checkout URL.
     */
    fun retryPayment(appointmentId: String) {
        viewModelScope.launch {
            try {
                val paymentLink = bookingRepository.createPaymentLinkForAppointment(appointmentId)
                _uiState.update { it.copy(paymentUrl = paymentLink.checkoutUrl) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể tạo liên kết thanh toán: ${e.message}") }
            }
        }
    }

    fun clearPaymentUrl() {
        _uiState.update { it.copy(paymentUrl = null) }
    }
}