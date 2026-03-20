package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.usecases.GetStoreProductByIdUseCase
import com.example.vetbook.domain.usecases.ObserveCartUseCase
import com.example.vetbook.domain.usecases.SetCartQuantityUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: StoreProduct? = null,
    val cartQuantity: Int = 0,
    val message: String? = null,
    val addedToCart: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductById: GetStoreProductByIdUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val setCartQuantityUseCase: SetCartQuantityUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var currentProductId: String? = null

    fun loadProduct(productId: String) {
        if (currentProductId == productId && _uiState.value.product != null) return
        currentProductId = productId
        val uid = auth.currentUser?.uid

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getProductById(productId).collect { product ->
                if (product == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Sản phẩm không tồn tại")
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, product = product) }
                }
            }
        }

        if (!uid.isNullOrBlank()) {
            viewModelScope.launch {
                observeCartUseCase(uid).collect { cartLines ->
                    val qty = cartLines.find { it.productId == productId }?.quantity ?: 0
                    _uiState.update { it.copy(cartQuantity = qty) }
                }
            }
        }
    }

    fun addToCart() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(message = "Vui lòng đăng nhập để thêm vào giỏ") }
            return
        }
        val productId = currentProductId ?: return
        viewModelScope.launch {
            val current = _uiState.value.cartQuantity
            val result = setCartQuantityUseCase(uid, productId, current + 1)
            _uiState.update {
                it.copy(
                    message = if (result.isSuccess) "Đã thêm vào giỏ hàng" else "Thêm vào giỏ hàng thất bại",
                    addedToCart = result.isSuccess
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, addedToCart = false) }
    }
}
