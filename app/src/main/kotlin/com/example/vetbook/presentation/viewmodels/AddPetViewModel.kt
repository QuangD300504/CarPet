package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.example.vetbook.domain.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val ageYears: String = "",
    val ageMonths: String = "",
    val weightKg: String = "",
    val note: String = "",
    
    val petId: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val existingImageUrl: String? = null
) {
    val isEditMode: Boolean get() = petId != null
}

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val petDataSource: RemotePetDataSource,
    private val imageRepository: ImageRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState: StateFlow<AddPetUiState> = _uiState.asStateFlow()

    fun setName(value: String) = _uiState.update { it.copy(name = value) }
    fun setType(value: String) = _uiState.update { it.copy(type = value) }
    fun setBreed(value: String) = _uiState.update { it.copy(breed = value) }
    fun setGender(value: String) = _uiState.update { it.copy(gender = value) }
    fun setAgeYears(value: String) = _uiState.update { it.copy(ageYears = value) }
    fun setAgeMonths(value: String) = _uiState.update { it.copy(ageMonths = value) }
    fun setWeightKg(value: String) = _uiState.update { it.copy(weightKg = value) }
    fun setNote(value: String) = _uiState.update { it.copy(note = value) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

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
                            ageYears = dto.age.substringBefore(" ").filter { it.isDigit() },
                            ageMonths = dto.age.substringAfter(" ", "").filter { it.isDigit() },
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

                val petDto = PetDto(
                    id = state.petId ?: "",
                    ownerId = uid,
                    name = state.name.trim(),
                    type = state.type,
                    breed = state.breed.trim(),
                    gender = state.gender,
                    age = "${state.ageYears}y ${state.ageMonths}m",
                    weight = state.weightKg.trim(),
                    note = state.note.trim(),
                    imageUrl = finalImageUrl,
                    updatedAt = System.currentTimeMillis()
                )

                val result = if (state.petId == null) {
                    petDataSource.createPet(petDto)
                } else {
                    petDataSource.updatePet(petDto)
                }

                if (result.isSuccess) {
                    _uiState.update { it.copy(isSaving = false, success = true) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }
}
