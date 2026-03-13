package com.example.vetbook.domain.repository

import com.example.vetbook.presentation.models.Accommodation

interface AccommodationRepository {
    suspend fun getAccommodations(): List<Accommodation>
    suspend fun getAccommodationById(id: String): Accommodation?
}
