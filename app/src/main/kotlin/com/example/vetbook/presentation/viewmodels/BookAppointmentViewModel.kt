package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BookAppointmentViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class PaymentReady(val appointmentId: String, val lockId: String, val checkoutUrl: String) : UiState()
    }

    private var pendingAppointmentId: String? = null
    private var pendingLockId: String? = null

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun confirmAndPay(
        veterinarianId: String,
        appointmentAt: Date,
        totalPrice: Double,
        durationMinutes: Int = 30,
        notes: String? = null
    ) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val created = bookingRepository.createAppointmentWithSlotLock(
                    veterinarianId = veterinarianId,
                    appointmentAt = appointmentAt,
                    totalPrice = totalPrice,
                    durationMinutes = durationMinutes,
                    notes = notes
                )
                pendingAppointmentId = created.appointmentId
                pendingLockId = created.lockId

                val payment = bookingRepository.createPaymentLinkForAppointment(created.appointmentId)
                _uiState.value = UiState.PaymentReady(
                    appointmentId = created.appointmentId,
                    lockId = created.lockId,
                    checkoutUrl = payment.checkoutUrl
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Booking failed")
            }
        }
    }

    fun clearPendingUnlock() {
        pendingAppointmentId = null
        pendingLockId = null
    }

    fun cancelAppointment(appointmentId: String, lockId: String) {
        clearPendingUnlock()
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                bookingRepository.cancelAppointment(appointmentId, lockId)
            } catch (e: Exception) {
                // Log or ignore since user cancelled anyway
            }
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        val apptId = pendingAppointmentId
        val lockId = pendingLockId
        if (apptId != null && lockId != null) {
            cancelAppointment(apptId, lockId)
        }
    }
}
