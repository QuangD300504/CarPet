package com.example.vetbook.store

import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.models.CartLineDto
import com.example.vetbook.data.models.StoreOrderDto
import com.example.vetbook.data.models.StoreProductDto
import com.example.vetbook.data.repository.FirebaseStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeRemoteStoreDataSource(
    initialProducts: List<StoreProductDto> = emptyList(),
    initialCart: List<CartLineDto> = emptyList()
) : RemoteStoreDataSource {
    private val productsFlow = MutableStateFlow(initialProducts)
    private val cartFlow = MutableStateFlow(initialCart)
    var cartCleared = false
        private set

    override fun observeProducts(): Flow<List<StoreProductDto>> = productsFlow

    override fun observeProductsByCategory(category: String): Flow<List<StoreProductDto>> {
        return MutableStateFlow(productsFlow.value.filter { it.category == category })
    }

    override fun observeUserCart(uid: String): Flow<List<CartLineDto>> = cartFlow

    override fun observeOrders(uid: String): Flow<List<StoreOrderDto>> = kotlinx.coroutines.flow.emptyFlow()

    override suspend fun getOrderById(orderId: String): StoreOrderDto? = null

    override suspend fun setCartQuantity(uid: String, productId: String, quantity: Int): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun clearCart(uid: String): Result<Unit> {
        cartFlow.value = emptyList()
        cartCleared = true
        return Result.success(Unit)
    }

    override suspend fun addProduct(product: StoreProductDto): Result<String> {
        val current = productsFlow.value.toMutableList()
        current.add(product)
        productsFlow.value = current
        return Result.success(product.id)
    }

    override suspend fun updateProduct(product: StoreProductDto): Result<Unit> {
        val current = productsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            current[index] = product
            productsFlow.value = current
        }
        return Result.success(Unit)
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        val current = productsFlow.value.toMutableList()
        current.removeAll { it.id == productId }
        productsFlow.value = current
        return Result.success(Unit)
    }
}

class StoreRepositoryTest {

    @Test
    fun `observeProducts maps dto to domain`() = runTest {
        val ds = FakeRemoteStoreDataSource(
            initialProducts = listOf(
                StoreProductDto(id = "p1", name = "Pate", price = 40.0, imageUrl = "https://x", category = "foods")
            )
        )
        val repo = FirebaseStoreRepository(ds)

        val items = repo.observeProducts().first()
        assertEquals(1, items.size)
        assertEquals("p1", items[0].id)
        assertEquals("Pate", items[0].name)
        assertEquals(40.0, items[0].price, 0.0001)
        assertEquals("foods", items[0].category)
    }

    @Test
    fun `clearCart delegates to data source and returns success`() = runTest {
        val ds = FakeRemoteStoreDataSource(
            initialCart = listOf(
                CartLineDto(productId = "p1", quantity = 2),
                CartLineDto(productId = "p2", quantity = 1)
            )
        )
        val repo = FirebaseStoreRepository(ds)

        val result = repo.clearCart("uid123")

        assertTrue(result.isSuccess)
        assertTrue(ds.cartCleared)
        assertEquals(emptyList<CartLineDto>(), ds.observeUserCart("uid123").first())
    }
}

