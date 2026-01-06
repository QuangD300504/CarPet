package com.example.carpet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carpet.domain.models.PetServiceDetail
import com.example.carpet.domain.models.ServiceCategory
import com.example.carpet.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ServiceDetailViewModel(
    private val repository: ServiceRepository,
    private val categoryId: String
) : ViewModel() {
    private val _detail = MutableStateFlow<PetServiceDetail?>(null)
    val detail: StateFlow<PetServiceDetail?> = _detail

    private val _category = MutableStateFlow<ServiceCategory?>(null)
    val category: StateFlow<ServiceCategory?> = _category

    init {
        _category.value = repository.getCategories().find { it.id == categoryId }
        _detail.value = repository.getServiceDetail(categoryId)
    }
}

class ServiceDetailViewModelFactory(
    private val repository: ServiceRepository,
    private val categoryId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ServiceDetailViewModel(repository, categoryId) as T
    }
}