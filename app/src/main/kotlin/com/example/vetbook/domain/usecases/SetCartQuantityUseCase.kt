package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.repository.StoreRepository

class SetCartQuantityUseCase(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(uid: String, productId: String, quantity: Int): Result<Unit> {
        return storeRepository.setCartQuantity(uid, productId, quantity)
    }
}

