package com.example.vetbook.presentation.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminAppointmentsUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminAppointmentsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookingRepository.getAllAppointments()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { appointments ->
                    _uiState.update { it.copy(appointments = appointments, isLoading = false) }
                }
        }
    }
}
