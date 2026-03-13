package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.domain.usecases.GetAccommodationByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccommodationDetailViewModel @Inject constructor(
    private val getAccommodationByIdUseCase: GetAccommodationByIdUseCase
) : ViewModel() {

    private val _accommodation = MutableStateFlow<Accommodation?>(null)
    val accommodation: StateFlow<Accommodation?> = _accommodation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadAccommodation(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _accommodation.value = getAccommodationByIdUseCase(id)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
