package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.AccommodationDto

interface RemoteAccommodationDataSource {
    suspend fun getAccommodations(): List<AccommodationDto>
    suspend fun getAccommodationById(id: String): AccommodationDto?
}
