package com.example.vetbook.presentation.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Banner
import com.example.vetbook.domain.repository.BannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBannersUiState(
    val banners: List<Banner> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminBannersViewModel @Inject constructor(
    private val repository: BannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBannersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getBanners()
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { banners -> _uiState.update { it.copy(banners = banners, isLoading = false) } }
        }
    }

    fun deleteBanner(id: String) {
        viewModelScope.launch {
            repository.deleteBanner(id)
        }
    }
}
