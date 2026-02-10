package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.repository.BookingRepository
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
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                bookingRepository.getUserAppointments(userId).collect { appointments ->
                    _uiState.update { 
                        it.copy(
                            appointments = appointments,
                            isLoading = false
                        ) 
                    }
                }
            }
        }
    }

    fun onPreviousMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
    }

    fun onNextMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
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
}
