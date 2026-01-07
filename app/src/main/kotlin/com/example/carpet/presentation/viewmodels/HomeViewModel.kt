package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carpet.domain.models.ServiceCategory
import com.example.carpet.domain.repository.ServiceRepository
import com.example.carpet.presentation.models.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(private val repository: ServiceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _categories = MutableStateFlow<List<ServiceCategory>>(emptyList())
    val categories: StateFlow<List<ServiceCategory>> = _categories

    init {
        _categories.value = repository.getCategories()
    }
}

class HomeViewModelFactory(private val repository: ServiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}