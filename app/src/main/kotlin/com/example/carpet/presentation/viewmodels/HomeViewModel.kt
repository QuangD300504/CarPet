package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.carpet.domain.models.ServiceCategory
import com.example.carpet.domain.usecases.GetServiceCategoriesUseCase
import com.example.carpet.domain.repository.ServiceRepository
import com.example.carpet.presentation.models.HomeUiState
import com.example.carpet.utils.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val getServiceCategoriesUseCase: GetServiceCategoriesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _categories = MutableStateFlow<List<ServiceCategory>>(emptyList())
    val categories: StateFlow<List<ServiceCategory>> = _categories

    init {
        _categories.value = getServiceCategoriesUseCase()
    }
}

class HomeViewModelFactory(
    private val repository: ServiceRepository
) : ViewModelFactory<HomeViewModel>(
    create = { HomeViewModel(GetServiceCategoriesUseCase(repository)) },
    viewModelClass = HomeViewModel::class.java
)
