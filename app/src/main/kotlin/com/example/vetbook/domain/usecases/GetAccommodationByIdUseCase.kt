package com.example.vetbook.domain.usecases

import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.domain.repository.AccommodationRepository
import javax.inject.Inject

class GetAccommodationByIdUseCase @Inject constructor(
    private val repository: AccommodationRepository
) {
    suspend operator fun invoke(id: String): Accommodation? {
        return repository.getAccommodationById(id)
    }
}
