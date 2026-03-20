package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.StoreOrder
import com.example.vetbook.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrdersUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    operator fun invoke(uid: String): Flow<List<StoreOrder>> {
        return repository.observeOrders(uid)
    }
}
