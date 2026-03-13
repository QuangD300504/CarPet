package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetAccommodationsUseCase
import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.AccommodationUiState
import com.example.vetbook.presentation.models.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccommodationViewModel @Inject constructor(
    private val getAccommodationsUseCase: GetAccommodationsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccommodationUiState())
    val uiState: StateFlow<AccommodationUiState> = _uiState.asStateFlow()
    
    init {
        loadAccommodations()
    }
    
    private fun loadAccommodations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val accommodations = getAccommodationsUseCase()
                _uiState.update { 
                    it.copy(
                        accommodations = accommodations,
                        filteredAccommodations = applyFilters(
                            accommodations,
                            it.selectedCategory,
                            it.searchQuery
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun filterByCategory(category: AccommodationCategory?) {
        _uiState.update { state ->
            val selectedCategory = if (state.selectedCategory == category) null else category
            state.copy(
                selectedCategory = selectedCategory,
                filteredAccommodations = applyFilters(
                    state.accommodations,
                    selectedCategory,
                    state.searchQuery
                )
            )
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredAccommodations = applyFilters(
                    state.accommodations,
                    state.selectedCategory,
                    query
                )
            )
        }
    }
    
    fun toggleViewMode() {
        _uiState.update { state ->
            state.copy(
                viewMode = if (state.viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
            )
        }
    }
    
    private fun applyFilters(
        accommodations: List<Accommodation>,
        category: AccommodationCategory?,
        searchQuery: String
    ): List<Accommodation> {
        var filtered = accommodations
        
        // Filter by category
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }
        
        // Filter by search query - optimized with early return
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.location.lowercase().contains(query) ||
                it.district.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }
        
        return filtered
    }
}
