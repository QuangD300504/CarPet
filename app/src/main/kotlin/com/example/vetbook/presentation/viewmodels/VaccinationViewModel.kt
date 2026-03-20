package com.example.vetbook.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.repository.VaccinationRepository
import com.example.vetbook.notification.ReminderNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class VaccinationUiState(
    val vaccinations: List<Vaccination> = emptyList(),
    val upcoming: List<Vaccination> = emptyList(),
    val overdue: List<Vaccination> = emptyList(),
    val completed: List<Vaccination> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class VaccinationViewModel @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    savedStateHandle: SavedStateHandle,
    private val application: Application
) : ViewModel() {

    private val petId: String = savedStateHandle["petId"] ?: ""
    private val petName: String = savedStateHandle["petName"] ?: "Thú cưng"

    private val _uiState = MutableStateFlow(VaccinationUiState())
    val uiState: StateFlow<VaccinationUiState> = _uiState.asStateFlow()

    init {
        loadVaccinations()
        scheduleExistingReminders()
    }

    private fun scheduleExistingReminders() {
        viewModelScope.launch {
            vaccinationRepository.getVaccinationsForPet(petId).first().forEach { v ->
                scheduleReminder(v)
            }
        }
    }

    private fun scheduleReminder(vaccination: Vaccination) {
        if (!vaccination.reminderEnabled || vaccination.nextDueDate == null) return
        val reminderInstant = vaccination.nextDueDate
            .atZone(ZoneId.systemDefault())
            .minusDays(vaccination.reminderDaysBefore.toLong())
            .toInstant()
        val dueDateFormatted = vaccination.nextDueDate
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ReminderNotificationHelper.scheduleVaccinationReminder(
            context = application,
            workName = "vaccination_reminder_${vaccination.id}",
            petName = petName,
            vaccineName = vaccination.title,
            dueDate = dueDateFormatted,
            reminderTimeMillis = reminderInstant.toEpochMilli()
        )
    }

    fun loadVaccinations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                vaccinationRepository.getVaccinationsForPet(petId).collect { vaccinations ->
                    _uiState.update {
                        it.copy(
                            vaccinations = vaccinations,
                            upcoming = vaccinations.filter { v -> v.status == VaccinationStatus.SCHEDULED },
                            overdue = vaccinations.filter { v -> v.status == VaccinationStatus.OVERDUE },
                            completed = vaccinations.filter { v -> v.status == VaccinationStatus.COMPLETED },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải dữ liệu tiêm chủng: ${e.message}"
                    )
                }
            }
        }
    }

    fun addVaccination(vaccination: Vaccination) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = vaccinationRepository.addVaccination(vaccination)
            if (result.isSuccess) {
                result.getOrNull()?.let { scheduleReminder(it) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Đã thêm lịch tiêm chủng"
                    )
                }
                loadVaccinations()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể thêm: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun updateVaccination(vaccination: Vaccination) {
        viewModelScope.launch {
            // Cancel old reminder before scheduling new one
            ReminderNotificationHelper.cancelVaccinationReminder(
                application,
                "vaccination_reminder_${vaccination.id}"
            )
            val result = vaccinationRepository.updateVaccination(vaccination)
            if (result.isSuccess) {
                scheduleReminder(vaccination)
                _uiState.update {
                    it.copy(successMessage = "Đã cập nhật thông tin tiêm chủng")
                }
                loadVaccinations()
            } else {
                _uiState.update {
                    it.copy(error = "Không thể cập nhật: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun deleteVaccination(vaccinationId: String) {
        viewModelScope.launch {
            ReminderNotificationHelper.cancelVaccinationReminder(
                application,
                "vaccination_reminder_$vaccinationId"
            )
            val result = vaccinationRepository.deleteVaccination(vaccinationId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(successMessage = "Đã xóa lịch tiêm chủng")
                }
                loadVaccinations()
            } else {
                _uiState.update {
                    it.copy(error = "Không thể xóa: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun uploadCertificate(vaccinationId: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = vaccinationRepository.uploadCertificate(vaccinationId, imageBytes)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Đã tải lên chứng nhận"
                    )
                }
                loadVaccinations()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải lên: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}