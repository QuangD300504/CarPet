package com.example.vetbook.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.notification.ReminderNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookAppointmentViewModel @Inject constructor(
    private val application: Application,
    private val bookingRepository: BookingRepository,
    private val petDataSource: RemotePetDataSource,
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class PaymentReady(val appointmentId: String, val lockId: String, val checkoutUrl: String) : UiState()
    }

    var pendingAppointmentId: String? = null
        private set
    var pendingLockId: String? = null
        private set
    var pendingVetName: String? = null
    private set
var pendingPetName: String? = null
    private set
var pendingAppointmentAt: Date? = null
    private set

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _pets = MutableStateFlow<List<com.example.vetbook.domain.models.Pet>>(emptyList())
    val pets = _pets.asStateFlow()

    private val _selectedPetIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPetIds = _selectedPetIds.asStateFlow()

    val timeSlots = listOf("09:00", "09:30", "10:00", "10:30", "11:00", "14:00", "14:30", "15:00", "15:30")

    private val _bookedSlots = MutableStateFlow<Set<String>>(emptySet())
    val bookedSlots = _bookedSlots.asStateFlow()

    init {
        loadUserPets()
    }

    private fun loadUserPets() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userPets = petDataSource.getUserPets(uid).map { it.toDomain() }
                _pets.value = userPets
                if (userPets.isNotEmpty() && _selectedPetIds.value.isEmpty()) {
                    _selectedPetIds.value = setOf(userPets.first().id)
                }
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }

    fun togglePetSelection(petId: String) {
        _selectedPetIds.update { current ->
            if (current.contains(petId)) {
                if (current.size > 1) current - petId else current
            } else {
                current + petId
            }
        }
    }

    fun loadBookedSlots(veterinarianId: String, year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            try {
                val locked = bookingRepository.getLockedSlots(
                    veterinarianId = veterinarianId,
                    year = year,
                    month = month,
                    day = day
                )
                _bookedSlots.value = locked
            } catch (e: Exception) {
                _bookedSlots.value = emptySet()
            }
        }
    }

    fun confirmAndPay(
        veterinarianId: String,
        appointmentAt: Date,
        totalPrice: Double,
        durationMinutes: Int = 30,
        notes: String? = null,
        vetName: String = "Bác sĩ",
        petName: String = "Thú cưng"
    ) {
        _uiState.value = UiState.Loading
        pendingAppointmentAt = appointmentAt
        pendingVetName = vetName
        pendingPetName = petName
        viewModelScope.launch {
            try {
                val created = bookingRepository.createAppointmentWithSlotLock(
                    veterinarianId = veterinarianId,
                    appointmentAt = appointmentAt,
                    totalPrice = totalPrice,
                    durationMinutes = durationMinutes,
                    notes = notes,
                    petIds = _selectedPetIds.value.toList()
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

    fun onPaymentSuccess(appointmentId: String) {
        viewModelScope.launch {
            try {
                bookingRepository.markAppointmentAsPaid(appointmentId)
            } catch (e: Exception) {
                // Ignore failure
            }

            val apptAt = pendingAppointmentAt
            val vetName = pendingVetName ?: "Bác sĩ"
            val petName = pendingPetName ?: "Thú cưng"
            if (apptAt != null) {
                val reminderMillis = apptAt.time - (24 * 60 * 60 * 1000L) // 1 day before
                val apptFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(apptAt)
                ReminderNotificationHelper.scheduleAppointmentReminder(
                    context = application,
                    workName = "appointment_reminder_$appointmentId",
                    vetName = vetName,
                    petName = petName,
                    appointmentTime = apptFormatted,
                    reminderTimeMillis = reminderMillis
                )
            }

            pendingAppointmentId = null
            pendingLockId = null
            pendingAppointmentAt = null
            pendingVetName = null
            pendingPetName = null
        }
    }

    fun cancelAppointment(appointmentId: String, lockId: String) {
        pendingAppointmentId = null
        pendingLockId = null
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