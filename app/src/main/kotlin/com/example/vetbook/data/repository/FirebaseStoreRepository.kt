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

    override suspend fun addProduct(product: StoreProduct): Result<String> {
        return remoteStoreDataSource.addProduct(product.toDto())
    }

    override suspend fun updateProduct(product: StoreProduct): Result<Unit> {
        return remoteStoreDataSource.updateProduct(product.toDto())
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return remoteStoreDataSource.deleteProduct(productId)
    }
}

// Extension to map Domain to DTO safely
private fun StoreProduct.toDto(): com.example.vetbook.data.models.StoreProductDto {
    return com.example.vetbook.data.models.StoreProductDto(
        id = id,
        name = name,
        price = price,
        imageUrl = imageUrl,
        description = description,
        category = category,
        stock = stock,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

