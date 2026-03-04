package com.example.vetbook.presentation.viewmodels.admin

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.models.VeterinarianDto
import com.example.vetbook.data.network.CloudinaryConfig
import com.example.vetbook.data.network.CloudinaryService
import com.example.vetbook.domain.repository.VeterinarianRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class VetFormState(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val experience: String = "",
    val initials: String = "",
    val bio: String = "",
    val email: String = "",
    val phone: String = "",
    val imageUrl: String? = null,
    val localImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AdminVetViewModel @Inject constructor(
    private val repository: VeterinarianRepository,
    private val cloudinaryService: CloudinaryService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(VetFormState())
    val uiState = _uiState.asStateFlow()

    private val vetId: String? = savedStateHandle.get<String>("vetId")

    init {
        if (vetId != null && vetId != "new") {
            loadVet(vetId)
        }
    }

    private fun loadVet(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val vet = repository.getVeterinarianDtoById(id)
            if (vet != null) {
                _uiState.update {
                    it.copy(
                        id = vet.id,
                        name = vet.name,
                        specialty = vet.specialty,
                        experience = vet.experience,
                        initials = vet.initials,
                        bio = vet.bio,
                        email = vet.email,
                        phone = vet.phone,
                        imageUrl = vet.imageUrl,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Vet not found") }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onSpecialtyChange(value: String) = _uiState.update { it.copy(specialty = value) }
    fun onExperienceChange(value: String) = _uiState.update { it.copy(experience = value) }
    fun onInitialsChange(value: String) = _uiState.update { it.copy(initials = value) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onImageSelected(uri: Uri?) = _uiState.update { it.copy(localImageUri = uri, imageUrl = null) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun saveVet(imageBytes: ByteArray?) {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.name.isBlank() || currentState.specialty.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name and Specialty are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            var uploadedUrl = currentState.imageUrl

            if (imageBytes != null) {
                try {
                    val mediaType = "image/*".toMediaTypeOrNull()
                    val requestBody = imageBytes.toRequestBody(mediaType)
                    val part = MultipartBody.Part.createFormData("file", "vet_image.jpg", requestBody)
                    val presetBody = CloudinaryConfig.UPLOAD_PRESET_VETBOOK.toRequestBody("text/plain".toMediaType())
                    val apiKeyBody = CloudinaryConfig.API_KEY.toRequestBody("text/plain".toMediaType())

                    val response = cloudinaryService.uploadImage(
                        file = part,
                        uploadPreset = presetBody,
                        apiKey = apiKeyBody
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        uploadedUrl = response.body()!!.secure_url
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to upload image") }
                        return@launch
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Image upload error: ${e.message}") }
                    return@launch
                }
            }

            try {
                if (currentState.id.isNotBlank()) {
                    // Update existing
                    val updates = mapOf(
                        "name" to currentState.name,
                        "specialty" to currentState.specialty,
                        "experience" to currentState.experience,
                        "initials" to currentState.initials,
                        "bio" to currentState.bio,
                        "email" to currentState.email,
                        "phone" to currentState.phone,
                        "imageUrl" to uploadedUrl
                    )
                    repository.updateVeterinarian(currentState.id, updates)
                } else {
                    // Create new
                    val newVet = VeterinarianDto(
                        name = currentState.name,
                        specialty = currentState.specialty,
                        experience = currentState.experience,
                        initials = currentState.initials,
                        bio = currentState.bio,
                        email = currentState.email,
                        phone = currentState.phone,
                        imageUrl = uploadedUrl,
                        isActive = true,
                        rating = 0.0,
                        reviewsCount = 0
                    )
                    repository.createVeterinarian(newVet)
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to save vet: ${e.message}") }
            }
        }
    }
}
