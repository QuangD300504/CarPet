package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.CartLine
import com.example.vetbook.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow

class ObserveCartUseCase(
    private val storeRepository: StoreRepository
) {
    operator fun invoke(uid: String): Flow<List<CartLine>> {
        return storeRepository.observeCart(uid)
    }
}

