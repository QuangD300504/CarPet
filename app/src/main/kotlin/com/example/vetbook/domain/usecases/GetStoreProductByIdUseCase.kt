package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetStoreProductByIdUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    operator fun invoke(productId: String): Flow<StoreProduct?> {
        return repository.observeProducts(null)
            .map { products -> products.find { it.id == productId } }
    }
}
