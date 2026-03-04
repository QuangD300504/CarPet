package com.example.vetbook.presentation.viewmodels.admin

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.network.CloudinaryConfig
import com.example.vetbook.data.network.CloudinaryService
import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class ProductFormState(
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val category: String = "",
    val stock: String = "",
    val imageUrl: String? = null,
    val localImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val cloudinaryService: CloudinaryService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String? = savedStateHandle.get<String>("productId")

    private val _uiState = MutableStateFlow(ProductFormState())
    val uiState = _uiState.asStateFlow()

    init {
        productId?.let { id ->
            if (id != "new") {
                loadProduct(id)
            }
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val products = storeRepository.observeProducts().firstOrNull()
            val product = products?.find { it.id == id }

            if (product != null) {
                _uiState.update {
                    it.copy(
                        name = product.name,
                        price = product.price.toLong().toString(),
                        description = product.description ?: "",
                        category = product.category ?: "",
                        stock = product.stock.toString(),
                        imageUrl = product.imageUrl,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Product not found") }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onPriceChange(price: String) = _uiState.update { it.copy(price = price) }
    fun onDescriptionChange(description: String) = _uiState.update { it.copy(description = description) }
    fun onCategoryChange(category: String) = _uiState.update { it.copy(category = category) }
    fun onStockChange(stock: String) = _uiState.update { it.copy(stock = stock) }
    fun onImageSelected(uri: Uri?) = _uiState.update { it.copy(localImageUri = uri, imageUrl = null) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun saveProduct(imageBytes: ByteArray?) {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.name.isBlank() || currentState.price.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name and Price are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            var uploadedUrl = currentState.imageUrl

            // Upload image if a new local byte array is provided
            if (imageBytes != null) {
                try {
                    val mediaType = "image/*".toMediaTypeOrNull()
                    val requestBody = imageBytes.toRequestBody(mediaType)
                    val part = MultipartBody.Part.createFormData("file", "product_image.jpg", requestBody)
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

            val product = StoreProduct(
                id = productId?.takeIf { it != "new" } ?: "",
                name = currentState.name,
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                description = currentState.description,
                category = currentState.category,
                stock = currentState.stock.toIntOrNull() ?: 0,
                imageUrl = uploadedUrl
            )

            val result = if (productId == null || productId == "new") {
                storeRepository.addProduct(product)
            } else {
                storeRepository.updateProduct(product)
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Failed to save product") 
                }
            }
        }
    }
}
