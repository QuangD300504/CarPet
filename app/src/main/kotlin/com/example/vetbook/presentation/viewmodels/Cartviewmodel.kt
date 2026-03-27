package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.CartLine
import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.repository.StoreRepository
import com.example.vetbook.domain.usecases.GetStoreProductByIdUseCase
import com.example.vetbook.domain.usecases.ObserveCartUseCase
import com.example.vetbook.domain.usecases.SetCartQuantityUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(val product: StoreProduct, val quantity: Int)

data class CartUiState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val errorMessage: String? = null
) {
    val total: Double get() = items.sumOf { it.product.price * it.quantity }
    val itemCount: Int get() = items.sumOf { it.quantity }
}

sealed class CartEvent {
    data class ItemRemoved(val product: StoreProduct, val previousQty: Int) : CartEvent()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val setCartQuantityUseCase: SetCartQuantityUseCase,
    private val storeRepository: StoreRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CartEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CartEvent> = _events.asSharedFlow()

//    init {
//        val uid = auth.currentUser?.uid ?: return@launch
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//
//            // Combine the live cart lines with the live product catalogue so
//            // prices / names stay up to date without a separate getProductById call.
//            val productsFlow = storeRepository.observeProducts(null)
//            val cartFlow = observeCartUseCase(uid)
//
//            productsFlow.combine(cartFlow) { products, lines ->
//                val productMap = products.associateBy { it.id }
//                lines.mapNotNull { line ->
//                    val product = productMap[line.productId] ?: return@mapNotNull null
//                    CartItem(product, line.quantity)
//                }
//            }.collect { items ->
//                _uiState.update { it.copy(isLoading = false, items = items) }
//            }
//        }
//    }
init {
    viewModelScope.launch {
        // Sử dụng if để tránh dùng return trong lambda nếu không cần thiết
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            _uiState.update { it.copy(isLoading = true) }

            val productsFlow = storeRepository.observeProducts(null)
            val cartFlow = observeCartUseCase(uid)

            productsFlow.combine(cartFlow) { products, lines ->
                val productMap = products.associateBy { it.id }
                lines.mapNotNull { line ->
                    val product = productMap[line.productId] ?: return@mapNotNull null
                    CartItem(product, line.quantity)
                }
            }.collect { items ->
                _uiState.update { it.copy(isLoading = false, items = items) }
            }
        }
    }
}

    fun increment(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        val current = _uiState.value.items.find { it.product.id == productId }?.quantity ?: 0
        viewModelScope.launch { setCartQuantityUseCase(uid, productId, current + 1) }
    }

    fun decrement(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        val item = _uiState.value.items.find { it.product.id == productId } ?: return
        if (item.quantity <= 1) {
            _events.tryEmit(CartEvent.ItemRemoved(item.product, item.quantity))
            viewModelScope.launch { setCartQuantityUseCase(uid, productId, 0) }
        } else {
            viewModelScope.launch { setCartQuantityUseCase(uid, productId, item.quantity - 1) }
        }
    }

    fun undoRemove(productId: String, quantity: Int) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch { setCartQuantityUseCase(uid, productId, quantity) }
    }

    fun removeItem(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        val item = _uiState.value.items.find { it.product.id == productId }
        item?.let { _events.tryEmit(CartEvent.ItemRemoved(it.product, it.quantity)) }
        viewModelScope.launch { setCartQuantityUseCase(uid, productId, 0) }
    }
}