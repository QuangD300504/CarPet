package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.usecases.GetStoreProductsUseCase
import com.example.vetbook.domain.usecases.ObserveCartUseCase
import com.example.vetbook.domain.usecases.SetCartQuantityUseCase
import com.example.vetbook.presentation.models.Product
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class StoreUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val cartCount: Int = 0,
    val message: String? = null
)

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getStoreProductsUseCase: GetStoreProductsUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val setCartQuantityUseCase: SetCartQuantityUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")
    private val lastCartQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        observeStore()
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun setCategory(category: String?) {
        selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addToCart(productId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = _uiState.value.copy(message = "Please login to add to cart")
            return
        }
        viewModelScope.launch {
            val current = lastCartQuantities.value[productId] ?: 0
            val result = setCartQuantityUseCase(uid, productId, current + 1)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(message = "Added to cart")
            } else {
                _uiState.value = _uiState.value.copy(message = "Failed to add to cart")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeStore() {
        val uid = auth.currentUser?.uid

        val cartFlow = if (uid.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            observeCartUseCase(uid)
        }

        viewModelScope.launch {
            selectedCategory
                .combine(searchQuery) { category, query -> category to query }
                .flatMapLatest { (category, query) ->
                    getStoreProductsUseCase(category)
                        .combine(cartFlow) { products, cartLines ->
                            lastCartQuantities.value = cartLines.associate { it.productId to it.quantity }

                            val mapped = products.map { p ->
                                Product(
                                    id = p.id,
                                    name = p.name,
                                    price = p.price.toString(),
                                    imageUrl = p.imageUrl,
                                    description = p.description
                                )
                            }
                            val filtered = if (query.isBlank()) mapped else mapped.filter {
                                it.name.contains(query, ignoreCase = true)
                            }

                            StoreUiState(
                                isLoading = false,
                                products = filtered,
                                errorMessage = null,
                                selectedCategory = category,
                                searchQuery = query,
                                cartCount = cartLines.sumOf { it.quantity }
                            )
                        }
                        .onStart { emit(_uiState.value.copy(isLoading = true, errorMessage = null)) }
                }
                .catch { e ->
                    emit(
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load store"
                        )
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }
}

