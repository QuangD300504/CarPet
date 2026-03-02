package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.repository.StoreRepository

class ClearCartUseCase(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(uid: String): Result<Unit> {
        return storeRepository.clearCart(uid)
    }
}
