package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.models.VaccinationType
import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.domain.repository.VaccinationRepository
import com.example.vetbook.domain.repository.VeterinarianRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AddVaccinationUiState(
    val title: String = "",
    val type: VaccinationType = VaccinationType.CORE,
    val scheduledDate: Instant? = null,
    val manufacturer: String = "",
    val batchNumber: String = "",
    val clinicName: String = "",
    val veterinarianName: String = "",
    val veterinarianId: String? = null,
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 7,
    val veterinarians: List<Veterinarian> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddVaccinationViewModel @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val veterinarianRepository: VeterinarianRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = savedStateHandle["petId"] ?: ""

    private val _uiState = MutableStateFlow(AddVaccinationUiState())
    val uiState: StateFlow<AddVaccinationUiState> = _uiState.asStateFlow()

    fun setTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun setType(value: VaccinationType) = _uiState.update { it.copy(type = value) }
    fun setScheduledDate(value: Instant?) = _uiState.update { it.copy(scheduledDate = value) }
    fun setManufacturer(value: String) = _uiState.update { it.copy(manufacturer = value) }
    fun setBatchNumber(value: String) = _uiState.update { it.copy(batchNumber = value) }
    fun setClinicName(value: String) = _uiState.update { it.copy(clinicName = value) }

    fun setVeterinarianName(value: String) {
        _uiState.update {
            it.copy(
                veterinarianName = value,
                veterinarianId = null // clear selection when user types
            )
        }
        if (value.length >= 2) {
            searchVeterinarians(value)
        } else {
            _uiState.update { it.copy(veterinarians = emptyList()) }
        }
    }

    fun selectVeterinarian(vet: Veterinarian) {
        _uiState.update {
            it.copy(
                veterinarianName = vet.name,
                veterinarianId = vet.id,
                veterinarians = emptyList()
            )
        }
    }

    fun setNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun setReminderEnabled(value: Boolean) = _uiState.update { it.copy(reminderEnabled = value) }
    fun setReminderDaysBefore(value: Int) = _uiState.update { it.copy(reminderDaysBefore = value) }

    private fun searchVeterinarians(query: String) {
        viewModelScope.launch {
            veterinarianRepository.getVeterinarians().collect { allVets ->
                _uiState.update {
                    it.copy(
                        veterinarians = allVets.filter { vet ->
                            vet.name.contains(query, ignoreCase = true) ||
                            vet.specialty.contains(query, ignoreCase = true)
                        }.take(5)
                    )
                }
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên vaccine") }
            return
        }
        if (state.scheduledDate == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ngày tiêm") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val vaccination = Vaccination(
                    id = "",
                    petId = petId,
                    title = state.title.trim(),
                    type = state.type,
                    status = VaccinationStatus.SCHEDULED,
                    scheduledDate = state.scheduledDate,
                    manufacturer = state.manufacturer.takeIf { it.isNotBlank() },
                    batchNumber = state.batchNumber.takeIf { it.isNotBlank() },
                    clinicName = state.clinicName.takeIf { it.isNotBlank() },
                    veterinarianId = state.veterinarianId,
                    veterinarianName = state.veterinarianName.takeIf { it.isNotBlank() },
                    notes = state.notes.takeIf { it.isNotBlank() },
                    reminderEnabled = state.reminderEnabled,
                    reminderDaysBefore = state.reminderDaysBefore,
                    createdAt = Instant.now()
                )
                val result = vaccinationRepository.addVaccination(vaccination)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
