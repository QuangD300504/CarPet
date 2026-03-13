package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.repository.AccommodationRepository
import com.example.vetbook.presentation.models.Accommodation
import javax.inject.Inject

class GetAccommodationsUseCase @Inject constructor(
    private val repository: AccommodationRepository
) {
    suspend operator fun invoke(): List<Accommodation> {
        return repository.getAccommodations()
    }
}
