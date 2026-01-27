package com.example.vetbook.data.datasource

import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.Coordinates

/**
 * Simple in-memory data source for accommodations.
 * This is a presentation-layer feature, not part of the core domain model.
 * 
 * TODO: If accommodations become a core feature, migrate to Firestore with proper DTOs.
 */
object AccommodationDataSource {
    
    fun getAccommodations(): List<Accommodation> = listOf(
        Accommodation(
            id = "1",
            name = "Cozy Pet Homestay",
            category = AccommodationCategory.HOMESTAY,
            location = "123 Park Ave",
            district = "Downtown",
            rating = 4.8f,
            reviewCount = 120,
            price = 50.0,
            description = "Pet-friendly homestay with spacious backyard",
            coordinates = Coordinates(10.7769, 106.7009),
            isPopular = true
        ),
        Accommodation(
            id = "2",
            name = "Pet Paradise Apart",
            category = AccommodationCategory.APART,
            location = "456 Oak St",
            district = "Midtown",
            rating = 4.6f,
            reviewCount = 85,
            price = 75.0,
            description = "Modern apartment with pet amenities",
            coordinates = Coordinates(10.7870, 106.7100)
        ),
        Accommodation(
            id = "3",
            name = "Pawfect Coffee",
            category = AccommodationCategory.COFFEE,
            location = "789 Main Rd",
            district = "City Center",
            rating = 4.9f,
            reviewCount = 200,
            price = 15.0,
            priceUnit = "USD/visit",
            description = "Pet-friendly cafe with play area",
            coordinates = Coordinates(10.7750, 106.6950),
            isPopular = true
        ),
        Accommodation(
            id = "4",
            name = "Grand Pet Hotel",
            category = AccommodationCategory.HOTEL,
            location = "321 Luxury Blvd",
            district = "Uptown",
            rating = 4.7f,
            reviewCount = 150,
            price = 120.0,
            description = "5-star hotel with premium pet services",
            coordinates = Coordinates(10.7900, 106.7150)
        ),
        Accommodation(
            id = "5",
            name = "Urban Pet Homestay",
            category = AccommodationCategory.HOMESTAY,
            location = "555 Green St",
            district = "East Side",
            rating = 4.5f,
            reviewCount = 95,
            price = 45.0,
            description = "Quiet homestay with pet grooming services",
            coordinates = Coordinates(10.7700, 106.7200)
        ),
        Accommodation(
            id = "6",
            name = "Riverside Pet Apart",
            category = AccommodationCategory.APART,
            location = "888 River View",
            district = "Riverside",
            rating = 4.4f,
            reviewCount = 70,
            price = 65.0,
            description = "Scenic apartment near walking trails",
            coordinates = Coordinates(10.7650, 106.6900)
        ),
        Accommodation(
            id = "7",
            name = "Furry Friends Cafe",
            category = AccommodationCategory.COFFEE,
            location = "999 Happy Lane",
            district = "West End",
            rating = 4.8f,
            reviewCount = 180,
            price = 12.0,
            priceUnit = "USD/visit",
            description = "Cozy cafe with adoption events",
            coordinates = Coordinates(10.7800, 106.7000),
            isPopular = true
        ),
        Accommodation(
            id = "8",
            name = "Pet Comfort Hotel",
            category = AccommodationCategory.HOTEL,
            location = "111 Comfort Dr",
            district = "North District",
            rating = 4.6f,
            reviewCount = 110,
            price = 95.0,
            description = "Family-friendly hotel with pet daycare",
            coordinates = Coordinates(10.7950, 106.7050)
        ),
        Accommodation(
            id = "9",
            name = "Garden Pet Homestay",
            category = AccommodationCategory.HOMESTAY,
            location = "222 Garden Way",
            district = "Suburbs",
            rating = 4.9f,
            reviewCount = 140,
            price = 55.0,
            description = "Beautiful garden setting for pets to roam",
            coordinates = Coordinates(10.7600, 106.7250),
            isPopular = true
        )
    )
}
