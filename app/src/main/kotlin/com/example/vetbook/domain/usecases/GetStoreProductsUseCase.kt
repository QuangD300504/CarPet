package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.StoreProduct
import com.example.vetbook.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow

class GetStoreProductsUseCase(
    private val storeRepository: StoreRepository
) {
    operator fun invoke(category: String? = null): Flow<List<StoreProduct>> {
        return storeRepository.observeProducts(category)
    }
}

