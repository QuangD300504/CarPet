package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.CartLine
import com.example.vetbook.domain.models.StoreProduct
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeProducts(category: String? = null): Flow<List<StoreProduct>>
    fun observeCart(uid: String): Flow<List<CartLine>>
    suspend fun setCartQuantity(uid: String, productId: String, quantity: Int): Result<Unit>
    suspend fun clearCart(uid: String): Result<Unit>
}

