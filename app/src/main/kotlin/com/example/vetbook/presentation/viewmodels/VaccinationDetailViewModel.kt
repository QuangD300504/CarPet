package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.models.VaccinationStatus
import com.example.vetbook.domain.repository.VaccinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class VaccinationDetailViewModel @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val vaccinationId: String = savedStateHandle["vaccinationId"] ?: ""

    private val _vaccination = MutableStateFlow<Vaccination?>(null)
    val vaccination: StateFlow<Vaccination?> = _vaccination.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadVaccination()
    }

    fun loadVaccination() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = vaccinationRepository.getVaccinationById(vaccinationId)
                result.onSuccess { _vaccination.value = it }
                    .onFailure { _error.value = it.message }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCompleted() {
        val current = _vaccination.value ?: return
        val updated = current.copy(
            status = VaccinationStatus.COMPLETED,
            completedDate = Instant.now()
        )
        viewModelScope.launch {
            _isLoading.value = true
            val result = vaccinationRepository.updateVaccination(updated)
            result.onSuccess {
                _vaccination.value = updated
                _message.value = "Đã đánh dấu hoàn thành"
            }.onFailure {
                _error.value = "Không thể cập nhật: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun markSkipped() {
        val current = _vaccination.value ?: return
        val updated = current.copy(status = VaccinationStatus.SKIPPED)
        viewModelScope.launch {
            _isLoading.value = true
            val result = vaccinationRepository.updateVaccination(updated)
            result.onSuccess {
                _vaccination.value = updated
                _message.value = "Đã đánh dấu bỏ qua"
            }.onFailure {
                _error.value = "Không thể cập nhật: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun uploadCertificate(imageBytes: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vaccinationRepository.uploadCertificate(vaccinationId, imageBytes)
            result.onSuccess { url ->
                val current = _vaccination.value ?: return@launch
                _vaccination.value = current.copy(certificateUrl = url)
                _message.value = "Đã tải lên chứng nhận"
            }.onFailure {
                _error.value = "Không thể tải lên: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun deleteVaccination(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vaccinationRepository.deleteVaccination(vaccinationId)
            if (result.isSuccess) {
                onDeleted()
            } else {
                _error.value = "Không thể xóa: ${result.exceptionOrNull()?.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _message.value = null
    }
}
