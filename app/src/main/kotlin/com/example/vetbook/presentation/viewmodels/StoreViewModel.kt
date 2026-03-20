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
import android.util.Log

enum class SortOption(val label: String) {
    NEWEST("Mới nhất"),
    PRICE_LOW_TO_HIGH("Giá thấp → cao"),
    PRICE_HIGH_TO_LOW("Giá cao → thấp"),
    BEST_SELLING("Bán chạy")
}

data class StoreUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val cartCount: Int = 0,
    val message: String? = null,
    val sortOption: SortOption = SortOption.NEWEST,
    val priceRangeMin: Float = 0f,
    val priceRangeMax: Float = 5_000_000f,
    val inStockOnly: Boolean = false,
    val isFilterSheetVisible: Boolean = false
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
        android.util.Log.d("StoreViewModel", "setSearchQuery: '$query'")
        searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = option)
        selectedCategory.value = selectedCategory.value // re-trigger
    }

    fun setPriceRange(min: Float, max: Float) {
        _uiState.value = _uiState.value.copy(priceRangeMin = min, priceRangeMax = max)
        selectedCategory.value = selectedCategory.value
    }

    fun setInStockOnly(inStock: Boolean) {
        _uiState.value = _uiState.value.copy(inStockOnly = inStock)
        selectedCategory.value = selectedCategory.value
    }

    fun setFilterSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isFilterSheetVisible = visible)
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
                                    stock = p.stock,
                                    imageUrl = p.imageUrl,
                                    shopName = p.shopName,
                                    category = p.category ?: "",
                                    description = p.description,
                                    createdAt = p.createdAt
                                )
                            }
                            Log.d("StoreViewModel", "observeStore: raw products=${mapped.size}, category=$category, query='$query'")
                            val filtered = mapped.filter { p ->
                                val matchesCategory = category == null || p.category.equals(category, ignoreCase = true)
                                val matchesQuery = query.isBlank() || p.name.contains(query, ignoreCase = true)
                                Log.d("StoreViewModel", "filter check: name='${p.name}', matchesCategory=$matchesCategory, matchesQuery=$matchesQuery")
                                matchesCategory && matchesQuery
                            }
                            Log.d("StoreViewModel", "observeStore: filtered=${filtered.size} products")

                            val priceMin = _uiState.value.priceRangeMin
                            val priceMax = _uiState.value.priceRangeMax
                            val inStockOnly = _uiState.value.inStockOnly
                            val sortOption = _uiState.value.sortOption

                            val priceFiltered = filtered.filter { p ->
                                val price = p.price.toDoubleOrNull() ?: 0.0
                                price >= priceMin && price <= priceMax &&
                                    (!inStockOnly || p.stock > 0)
                            }

                            val sorted = when (sortOption) {
                                SortOption.NEWEST -> priceFiltered.sortedByDescending { it.createdAt?.let { ts -> ts } ?: 0L }
                                SortOption.PRICE_LOW_TO_HIGH -> priceFiltered.sortedBy { it.price.toDoubleOrNull() ?: Double.MAX_VALUE }
                                SortOption.PRICE_HIGH_TO_LOW -> priceFiltered.sortedByDescending { it.price.toDoubleOrNull() ?: 0.0 }
                                SortOption.BEST_SELLING -> priceFiltered // placeholder
                            }

                            StoreUiState(
                                isLoading = false,
                                products = sorted,
                                errorMessage = null,
                                selectedCategory = category,
                                searchQuery = query,
                                cartCount = cartLines.sumOf { it.quantity },
                                sortOption = sortOption,
                                priceRangeMin = priceMin,
                                priceRangeMax = priceMax,
                                inStockOnly = inStockOnly,
                                isFilterSheetVisible = _uiState.value.isFilterSheetVisible
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

