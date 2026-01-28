package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.CartLineDto
import com.example.vetbook.data.models.StoreProductDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source for Store (products + user cart).
 *
 * Collections:
 * - products/{productId}
 * - users/{uid}/cart/{productId}
 */
interface RemoteStoreDataSource {

    fun observeProducts(): Flow<List<StoreProductDto>>

    fun observeProductsByCategory(category: String): Flow<List<StoreProductDto>>

    fun observeUserCart(uid: String): Flow<List<CartLineDto>>

    suspend fun setCartQuantity(uid: String, productId: String, quantity: Int): Result<Unit>
}

