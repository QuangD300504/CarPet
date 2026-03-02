package com.example.vetbook.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.CartLine
import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.usecases.ClearCartUseCase
import com.example.vetbook.domain.usecases.GetStoreProductsUseCase
import com.example.vetbook.domain.usecases.ObserveCartUseCase
import com.example.vetbook.domain.usecases.SetCartQuantityUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val clearCartUseCase: ClearCartUseCase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val paymentWorkerApi: com.example.vetbook.data.network.PaymentWorkerApi
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
        val total: Double = 0.0,
        val checkoutSuccess: Boolean = false
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

    suspend fun fetchVnpayUrl(): String {
        val token = auth.currentUser?.getIdToken(true)?.await()?.token
            ?: throw IllegalStateException("Not authenticated — please log in again.")
        val total = _uiState.value.total
        val amount = kotlin.math.round(total).toLong()
        if (amount <= 0L) throw IllegalArgumentException("Cart is empty.")
        val resp = paymentWorkerApi.createVnpayLink(
            authorization = "Bearer $token",
            body = com.example.vetbook.data.network.PaymentWorkerApi.CreateVnpayLinkRequest(
                amount = amount,
                orderCode = System.currentTimeMillis(),
                description = "VetBook store order",
                locale = "vn"
            )
        )
        return resp.url.also { url ->
            if (url.isBlank()) throw IllegalStateException("Worker returned empty URL.")
        }
    }

    /**
     * Called when VNPAY reports SuccessBackAction.
     * Writes a store order document to Firestore, then clears the cart.
     */
    fun onCheckoutSuccess() {
        val uid = auth.currentUser?.uid ?: return
        val state = _uiState.value
        viewModelScope.launch {
            try {
                // Write order record to Firestore
            val orderCode = System.currentTimeMillis()
            val orderData = hashMapOf(
                "uid" to uid,
                "orderCode" to orderCode.toString(), // matches vnp_TxnRef for IPN lookup
                "items" to state.lines.map { line ->
                    mapOf(
                        "productId" to line.product.id,
                        "productName" to line.product.name,
                        "quantity" to line.quantity,
                        "lineTotal" to line.lineTotal
                    )
                },
                    "itemCount" to state.itemCount,
                    "subtotal" to state.subtotal,
                    "discount" to state.discount,
                    "deliveryCharges" to state.deliveryCharges,
                    "total" to state.total,
                    "status" to "PAID",
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("storeOrders").add(orderData).await()
                Log.d("CheckoutViewModel", "Order saved successfully")
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Failed to save order: ${e.message}")
                // Non-fatal: still proceed to clear cart and navigate
            }

            // Clear cart regardless of order-save result
            val result = clearCartUseCase(uid)
            if (result.isFailure) {
                Log.e("CheckoutViewModel", "Failed to clear cart: ${result.exceptionOrNull()?.message}")
            }

            _uiState.value = _uiState.value.copy(checkoutSuccess = true)
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
        val delivery = if (lines.isEmpty()) 0.0 else 20000.0
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
