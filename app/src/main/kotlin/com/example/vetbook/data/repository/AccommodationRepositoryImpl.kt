package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemoteAccommodationDataSource
import com.example.vetbook.data.models.AccommodationDto
import com.example.vetbook.domain.repository.AccommodationRepository
import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.Coordinates
import javax.inject.Inject

class AccommodationRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAccommodationDataSource
) : AccommodationRepository {

    override suspend fun getAccommodations(): List<Accommodation> {
        return remoteDataSource.getAccommodations().map { it.toDomain() }
    }

    override suspend fun getAccommodationById(id: String): Accommodation? {
        return remoteDataSource.getAccommodationById(id)?.toDomain()
    }

    private fun AccommodationDto.toDomain(): Accommodation {
        return Accommodation(
            id = id,
            name = name,
            category = try {
                AccommodationCategory.valueOf(category.uppercase())
            } catch (e: Exception) {
                AccommodationCategory.HOTEL
            },
            location = location,
            district = district,
            rating = rating,
            reviewCount = reviewCount,
            price = price,
            priceUnit = priceUnit,
            imageUrl = imageUrl,
            description = description,
            coordinates = if (latitude != null && longitude != null) {
                Coordinates(latitude, longitude)
            } else null,
            isPopular = isPopular
        )
    }
}
