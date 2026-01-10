package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.usecases.GetServiceCategoriesUseCase
import com.example.vetbook.presentation.models.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
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
