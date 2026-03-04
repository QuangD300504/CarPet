package com.example.vetbook.presentation.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrdersUiState(
    val orders: List<Map<String, Any?>> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminOrdersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrdersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            callbackFlow {
                val sub = firestore.collection("orders")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snap, err ->
                        if (err != null) { close(err); return@addSnapshotListener }
                        val orders = snap?.documents?.map { doc ->
                            val data = doc.data?.toMutableMap() ?: mutableMapOf()
                            data["id"] = doc.id
                            data as Map<String, Any?>
                        } ?: emptyList()
                        trySend(orders)
                    }
                awaitClose { sub.remove() }
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { orders -> _uiState.update { it.copy(orders = orders, isLoading = false) } }
        }
    }
}
