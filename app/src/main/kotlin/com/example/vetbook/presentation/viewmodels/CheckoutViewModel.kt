package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.CartLine
import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.usecases.GetStoreProductsUseCase
import com.example.vetbook.domain.usecases.ObserveCartUseCase
import com.example.vetbook.domain.usecases.SetCartQuantityUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CheckoutViewModel owns cart + checkout summary state.
 * StoreViewModel remains focused on browsing/searching products.
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val setCartQuantityUseCase: SetCartQuantityUseCase,
    private val getStoreProductsUseCase: GetStoreProductsUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    data class UiLine(
        val product: StoreProduct,
        val quantity: Int
    ) {
        val lineTotal: Double get() = product.price * quantity
    }

    data class UiState(
        val isLoading: Boolean = true,
        val lines: List<UiLine> = emptyList(),
        val errorMessage: String? = null,
        val itemCount: Int = 0,
        val subtotal: Double = 0.0,
        val discount: Double = 0.0,
        val deliveryCharges: Double = 0.0,
        val total: Double = 0.0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observe()
    }

    fun setQuantity(productId: String, quantity: Int) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            setCartQuantityUseCase(uid, productId, quantity)
        }
    }

    private fun observe() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.value = UiState(isLoading = false)
            return
        }

        viewModelScope.launch {
            val cartFlow = observeCartUseCase(uid)
            val productsFlow = getStoreProductsUseCase(null)

            productsFlow
                .combine(cartFlow) { products: List<StoreProduct>, cart: List<CartLine> ->
                    buildState(products, cart)
                }
                .onStart { emit(_uiState.value.copy(isLoading = true, errorMessage = null)) }
                .catch { e ->
                    emit(
                        UiState(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load checkout"
                        )
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun buildState(products: List<StoreProduct>, cart: List<CartLine>): UiState {
        val quantitiesById = cart.associate { it.productId to it.quantity }
        val lines = products.mapNotNull { p ->
            val qty = quantitiesById[p.id] ?: return@mapNotNull null
            UiLine(product = p, quantity = qty)
        }

        val itemCount = lines.sumOf { it.quantity }
        val subtotal = lines.sumOf { it.lineTotal }
        val discount = 0.0
        val delivery = if (lines.isEmpty()) 0.0 else 2.0
        val total = subtotal - discount + delivery

        return UiState(
            isLoading = false,
            lines = lines,
            errorMessage = null,
            itemCount = itemCount,
            subtotal = subtotal,
            discount = discount,
            deliveryCharges = delivery,
            total = total
        )
    }
}
