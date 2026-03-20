package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.OrderStatus
import com.example.vetbook.domain.models.StoreOrder
import com.example.vetbook.domain.repository.StoreRepository
import com.example.vetbook.domain.usecases.ObserveOrdersUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderHistoryUiState(
    val isLoading: Boolean = true,
    val allOrders: List<StoreOrder> = emptyList(),
    val filteredOrders: List<StoreOrder> = emptyList(),
    val selectedTab: OrderHistoryTab = OrderHistoryTab.ALL,
    val errorMessage: String? = null
)

enum class OrderHistoryTab(val label: String) {
    ALL("Tất cả"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy")
}

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val storeRepository: StoreRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init { loadOrders() }

    private fun loadOrders() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.value = OrderHistoryUiState(
                isLoading = false,
                errorMessage = "Vui lòng đăng nhập"
            )
            return
        }
        viewModelScope.launch {
            observeOrdersUseCase(uid).collect { orders ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allOrders = orders,
                    filteredOrders = applyFilter(orders, _uiState.value.selectedTab),
                    errorMessage = null
                )
            }
        }
    }

    suspend fun getOrderById(orderId: String): StoreOrder? {
        // First check cached orders
        val cached = _uiState.value.allOrders.find { it.id == orderId }
        if (cached != null) return cached
        // Fallback: fetch directly from Firestore
        return storeRepository.getOrderById(orderId)
    }

    fun selectTab(tab: OrderHistoryTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            filteredOrders = applyFilter(_uiState.value.allOrders, tab)
        )
    }

    private fun applyFilter(orders: List<StoreOrder>, tab: OrderHistoryTab): List<StoreOrder> {
        return when (tab) {
            OrderHistoryTab.ALL -> orders
            OrderHistoryTab.PROCESSING -> orders.filter {
                it.status == OrderStatus.PENDING || it.status == OrderStatus.PAID
            }
            OrderHistoryTab.COMPLETED -> orders.filter {
                it.status == OrderStatus.SHIPPED || it.status == OrderStatus.DELIVERED
            }
            OrderHistoryTab.CANCELLED -> orders.filter {
                it.status == OrderStatus.CANCELLED
            }
        }
    }
}
