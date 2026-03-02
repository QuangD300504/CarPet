package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.domain.models.CartLine
import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseStoreRepository(
    private val remoteStoreDataSource: RemoteStoreDataSource
) : StoreRepository {

    override fun observeProducts(category: String?): Flow<List<StoreProduct>> {
        val flow = if (category.isNullOrBlank()) {
            remoteStoreDataSource.observeProducts()
        } else {
            remoteStoreDataSource.observeProductsByCategory(category)
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun observeCart(uid: String): Flow<List<CartLine>> {
        return remoteStoreDataSource.observeUserCart(uid).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun setCartQuantity(uid: String, productId: String, quantity: Int): Result<Unit> {
        return remoteStoreDataSource.setCartQuantity(uid, productId, quantity)
    }

    override suspend fun clearCart(uid: String): Result<Unit> {
        return remoteStoreDataSource.clearCart(uid)
    }
}

