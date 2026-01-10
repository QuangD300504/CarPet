package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.vetbook.domain.models.PetServiceDetail
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val repository: ServiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val categoryId: String = checkNotNull(savedStateHandle["serviceId"])

    private val _detail = MutableStateFlow<PetServiceDetail?>(null)
    val detail: StateFlow<PetServiceDetail?> = _detail

    private val _category = MutableStateFlow<ServiceCategory?>(null)
    val category: StateFlow<ServiceCategory?> = _category

    init {
        _category.value = repository.getCategories().find { it.id == categoryId }
        _detail.value = repository.getServiceDetail(categoryId)
    }
}
