package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.Vaccination
import com.example.vetbook.domain.repository.ImageRepository
import com.example.vetbook.domain.repository.NotificationRepository
import com.example.vetbook.domain.repository.VaccinationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AddPetUiState(
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,

    val name: String = "",
    val type: String = "Chó",
    val breed: String = "",
    val gender: String = "Đực",
    val birthDateMillis: Long? = null,
    val weightKg: String = "",
    val note: String = "",

    val petId: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val existingImageUrl: String? = null,

    /** After saving a new pet, holds the pet + generated vaccine records pending review */
    val pendingVaccinePet: Pet? = null,
    val pendingVaccineRecords: List<Vaccination> = emptyList()
) {
    val isEditMode: Boolean get() = petId != null
    /** True when VaccineReviewModal should be shown after a new pet is saved */
    val showVaccineReview: Boolean get() = pendingVaccinePet != null && pendingVaccineRecords.isNotEmpty()
}

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val application: Application,
    private val petDataSource: RemotePetDataSource,
    private val imageRepository: ImageRepository,
    private val vaccinationRepository: VaccinationRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState: StateFlow<AddPetUiState> = _uiState.asStateFlow()

    fun setName(value: String) = _uiState.update { it.copy(name = value) }
    fun setType(value: String) = _uiState.update { it.copy(type = value) }
    fun setBreed(value: String) = _uiState.update { it.copy(breed = value) }
    fun setGender(value: String) = _uiState.update { it.copy(gender = value) }
    fun setBirthDate(millis: Long) = _uiState.update { it.copy(birthDateMillis = millis) }
    fun setWeightKg(value: String) = _uiState.update { it.copy(weightKg = value) }
    fun setNote(value: String) = _uiState.update { it.copy(note = value) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    /**
     * Resets state for a fresh "add new pet" screen.
     * Called when navigating to AddPetScreen without a petId argument.
     */
    fun resetToNewPet() {
        _uiState.update {
            AddPetUiState()
        }
    }

    fun onImageSelected(bytes: ByteArray) = _uiState.update { it.copy(selectedImageBytes = bytes) }

    fun loadPet(petId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = petDataSource.getPetById(petId)
                if (result.isSuccess) {
                    val dto = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            petId = dto.id,
                            name = dto.name,
                            type = dto.type,
                            breed = dto.breed,
                            gender = dto.gender,
                            birthDateMillis = dto.birthDate,
                            weightKg = dto.weight,
                            note = dto.note,
                            existingImageUrl = dto.imageUrl
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên thú cưng") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val uid = auth.currentUser?.uid ?: throw IllegalStateException("Chưa đăng nhập")

                var finalImageUrl = state.existingImageUrl
                state.selectedImageBytes?.let { bytes ->
                    val uploadResult = imageRepository.uploadImage(bytes)
                    if (uploadResult.isSuccess) {
                        finalImageUrl = uploadResult.getOrNull()
                    } else {
                        throw uploadResult.exceptionOrNull() ?: Exception("Lỗi tải ảnh")
                    }
                }

                // Compute birthDate from age fields
                val birthDate = state.birthDateMillis

                val petDto = PetDto(
                    id = state.petId ?: "",
                    ownerId = uid,
                    name = state.name.trim(),
                    type = state.type,
                    breed = state.breed.trim(),
                    gender = state.gender,
                    age = "",
                    weight = state.weightKg.trim(),
                    note = state.note.trim(),
                    imageUrl = finalImageUrl,
                    birthDate = birthDate,
                    updatedAt = System.currentTimeMillis()
                )

                val isNewPet = state.petId == null
                if (isNewPet) {
                    val result = petDataSource.createPet(petDto)
                    if (result.isFailure) {
                        _uiState.update { it.copy(isSaving = false, errorMessage = result.exceptionOrNull()?.message) }
                        return@launch
                    }
                    val savedPet = result.getOrNull()!!
                    val savedPetId = savedPet.id
                    _uiState.update { it.copy(isSaving = false, success = true) }

                    val pet = Pet(
                        id = savedPetId,
                        ownerId = uid,
                        name = state.name.trim(),
                        type = state.type,
                        breed = state.breed.trim(),
                        birthDate = birthDate?.let { Instant.ofEpochMilli(it) }
                    )
                    val generated = vaccinationRepository.generateSchedule(pet)
                    if (generated.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                pendingVaccinePet = pet,
                                pendingVaccineRecords = generated
                            )
                        }
                    } else {
                        onSuccess()
                    }
                } else {
                    val result = petDataSource.updatePet(petDto)
                    if (result.isFailure) {
                        _uiState.update { it.copy(isSaving = false, errorMessage = result.exceptionOrNull()?.message) }
                        return@launch
                    }
                    _uiState.update { it.copy(isSaving = false, success = true) }
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    /**
     * User confirmed the vaccine review — save only the selected records,
     * delete the ones that were toggled off.
     */
    fun confirmVaccineReview(selectedRecords: List<Vaccination>, onComplete: () -> Unit) {
        val petId = _uiState.value.pendingVaccinePet?.id ?: return
        viewModelScope.launch {
            
selectedRecords.forEach { record ->
    val result = vaccinationRepository.addVaccination(record)
result.getOrNull()?.let { saved ->
    val dueDate = saved.scheduledDate ?: return@let
    val reminderTimeMillis = System.currentTimeMillis() + 10_000L
    val dueDateFormatted = dueDate
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    android.util.Log.d("VetBook", "AddPet scheduling reminder for ${saved.title}")
    com.example.vetbook.notification.ReminderNotificationHelper.scheduleVaccinationReminder(
        context = application,
        workName = "vaccination_reminder_${saved.id}",
        petName = saved.petName,
        vaccineName = saved.title,
        dueDate = dueDateFormatted,
        reminderTimeMillis = reminderTimeMillis
    )
}
}
            // Delete all other generated records
            val selectedIds = selectedRecords.map { it.id }.toSet()
            _uiState.value.pendingVaccineRecords
                .filter { it.id !in selectedIds }
                .forEach { record ->
                    vaccinationRepository.deleteVaccination(record.id)
                }
            _uiState.update { it.copy(pendingVaccinePet = null, pendingVaccineRecords = emptyList()) }
            onComplete()
        }
    }

    /**
     * User skipped/closed the review modal — delete all generated records.
     */
    fun skipVaccineReview(onComplete: () -> Unit) {
        val petId = _uiState.value.pendingVaccinePet?.id ?: return
        viewModelScope.launch {
            vaccinationRepository.deleteVaccinationsForPet(petId)
            _uiState.update { it.copy(pendingVaccinePet = null, pendingVaccineRecords = emptyList()) }
            onComplete()
        }
    }

    /**
     * Estimates birth date from age-in-years and age-in-months fields.
     * A pet "2 months old" was born 2 months ago → subtract from now.
     * Returns epoch millis (null if both fields are blank/zero).
     */
    // private fun computeBirthDate(years: String, months: String): Long? {
    //     val y = years.toIntOrNull() ?: 0
    //     val m = months.toIntOrNull() ?: 0
    //     if (y == 0 && m == 0) return null
    //     // "Pet is X years Y months old" → born X years Y months AGO
    //     val birthDate = LocalDate.now()
    //         .minusYears(y.toLong())
    //         .minusMonths(m.toLong())
    //     return birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    // }
}
