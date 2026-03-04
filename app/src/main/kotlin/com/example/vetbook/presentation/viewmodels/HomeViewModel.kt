package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.Banner
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.repository.BannerRepository
import com.example.vetbook.domain.usecases.GetServiceCategoriesUseCase
import com.example.vetbook.presentation.models.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getServiceCategoriesUseCase: GetServiceCategoriesUseCase,
    private val bannerRepository: BannerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _allCategories = MutableStateFlow<List<ServiceCategory>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val categories: StateFlow<List<ServiceCategory>>
        get() = _categories
    private val _categories = MutableStateFlow<List<ServiceCategory>>(emptyList())

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    init {
        val all = getServiceCategoriesUseCase()
        _allCategories.value = all
        _categories.value = all

        viewModelScope.launch {
            bannerRepository.getBanners()
                .catch { /* silent fail — banners are optional UI */ }
                .collect { _banners.value = it }
        }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        _categories.value = if (query.isBlank()) {
            _allCategories.value
        } else {
            _allCategories.value.filter { cat ->
                cat.title.contains(query, ignoreCase = true) ||
                cat.shortDescription.contains(query, ignoreCase = true)
            }
        }
    }
}
