package com.example.vetbook.store

import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.models.CartLineDto
import com.example.vetbook.data.models.StoreProductDto
import com.example.vetbook.data.repository.FirebaseStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeRemoteStoreDataSource(
    initialProducts: List<StoreProductDto> = emptyList(),
    initialCart: List<CartLineDto> = emptyList()
) : RemoteStoreDataSource {
    private val productsFlow = MutableStateFlow(initialProducts)
    private val cartFlow = MutableStateFlow(initialCart)

    override fun observeProducts(): Flow<List<StoreProductDto>> = productsFlow

    override fun observeProductsByCategory(category: String): Flow<List<StoreProductDto>> {
        return MutableStateFlow(productsFlow.value.filter { it.category == category })
    }

    override fun observeUserCart(uid: String): Flow<List<CartLineDto>> = cartFlow

    override suspend fun setCartQuantity(uid: String, productId: String, quantity: Int): Result<Unit> {
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
}

