package com.example.vetbook.presentation.viewmodels.admin

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Banner
import com.example.vetbook.domain.repository.BannerRepository
import com.example.vetbook.data.network.CloudinaryConfig
import com.example.vetbook.data.network.CloudinaryService
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

data class BannerFormState(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val targetUrl: String = "",
    val sortOrder: String = "0",
    val localImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminAddEditBannerViewModel @Inject constructor(
    private val repository: BannerRepository,
    private val cloudinaryService: CloudinaryService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BannerFormState())
    val uiState = _uiState.asStateFlow()

    private val bannerId: String? = savedStateHandle.get<String>("bannerId")

    init {
        if (!bannerId.isNullOrBlank() && bannerId != "new") {
            viewModelScope.launch {
                val banner = repository.getBannerById(bannerId)
                if (banner != null) {
                    _uiState.update {
                        it.copy(
                            id = banner.id, title = banner.title, subtitle = banner.subtitle,
                            imageUrl = banner.imageUrl, targetUrl = banner.targetUrl,
                            sortOrder = banner.sortOrder.toString()
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onSubtitleChange(v: String) = _uiState.update { it.copy(subtitle = v) }
    fun onTargetUrlChange(v: String) = _uiState.update { it.copy(targetUrl = v) }
    fun onSortOrderChange(v: String) = _uiState.update { it.copy(sortOrder = v) }
    fun onImageSelected(uri: Uri?) = _uiState.update { it.copy(localImageUri = uri, imageUrl = "") }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun saveBanner(imageBytes: ByteArray?) {
        val state = _uiState.value
        if (state.title.isBlank()) { _uiState.update { it.copy(errorMessage = "Title is required") }; return }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            var url = state.imageUrl

            if (imageBytes != null) {
                try {
                    val part = MultipartBody.Part.createFormData(
                        "file", "banner.jpg", imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                    )
                    val resp = cloudinaryService.uploadImage(
                        file = part,
                        uploadPreset = CloudinaryConfig.UPLOAD_PRESET_VETBOOK.toRequestBody("text/plain".toMediaType()),
                        apiKey = CloudinaryConfig.API_KEY.toRequestBody("text/plain".toMediaType())
                    )
                    if (resp.isSuccessful) url = resp.body()?.secure_url ?: url
                    else { _uiState.update { it.copy(isLoading = false, errorMessage = "Image upload failed") }; return@launch }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }; return@launch
                }
            }

            try {
                val banner = Banner(
                    id = state.id, title = state.title, subtitle = state.subtitle,
                    imageUrl = url, targetUrl = state.targetUrl,
                    sortOrder = state.sortOrder.toIntOrNull() ?: 0, isActive = true
                )
                if (state.id.isBlank()) repository.addBanner(banner)
                else repository.updateBanner(state.id, mapOf(
                    "title" to state.title, "subtitle" to state.subtitle,
                    "imageUrl" to url, "targetUrl" to state.targetUrl,
                    "sortOrder" to (state.sortOrder.toIntOrNull() ?: 0)
                ))
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
