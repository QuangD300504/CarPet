package com.example.vetbook.data.repository

import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.presentation.models.AccommodationCategory
import com.example.vetbook.presentation.models.Coordinates

class MockAccommodationRepository {
    fun getAccommodations(): List<Accommodation> {
        return listOf(
            // Homestay
            Accommodation(
                id = "homestay_1",
                name = "Homestay mèo mèo",
                category = AccommodationCategory.HOMESTAY,
                location = "Quận 3",
                district = "Quận 3",
                rating = 4.8f,
                reviewCount = 112,
                price = 40.0,
                description = "Chào mừng bạn đến với mèo mèo",
                coordinates = Coordinates(10.7769, 106.7009),
                isPopular = true
            ),
            Accommodation(
                id = "homestay_2",
                name = "Pet Friendly Homestay",
                category = AccommodationCategory.HOMESTAY,
                location = "Quận 1",
                district = "Quận 1",
                rating = 4.6f,
                reviewCount = 89,
                price = 45.0,
                description = "Comfortable homestay perfect for pet owners",
                coordinates = Coordinates(10.7769, 106.7009),
                isPopular = false
            ),
            
            // Apart
            Accommodation(
                id = "apart_1",
                name = "Modern Pet Apartment",
                category = AccommodationCategory.APART,
                location = "Quận 2",
                district = "Quận 2",
                rating = 4.7f,
                reviewCount = 156,
                price = 60.0,
                description = "Spacious apartment with pet-friendly amenities",
                coordinates = Coordinates(10.7870, 106.7490),
                isPopular = true
            ),
            Accommodation(
                id = "apart_2",
                name = "Luxury Pet Suite",
                category = AccommodationCategory.APART,
                location = "Quận 7",
                district = "Quận 7",
                rating = 4.9f,
                reviewCount = 203,
                price = 85.0,
                description = "Premium apartment for you and your pets",
                coordinates = Coordinates(10.7314, 106.7214),
                isPopular = false
            ),
            
            // Coffee
            Accommodation(
                id = "coffee_1",
                name = "Pet Cafe & Stay",
                category = AccommodationCategory.COFFEE,
                location = "Quận 4",
                district = "Quận 4",
                rating = 4.5f,
                reviewCount = 67,
                price = 35.0,
                description = "Coffee shop with pet accommodation",
                coordinates = Coordinates(10.7570, 106.7010),
                isPopular = false
            ),
            Accommodation(
                id = "coffee_2",
                name = "Cat Cafe Homestay",
                category = AccommodationCategory.COFFEE,
                location = "Quận 5",
                district = "Quận 5",
                rating = 4.8f,
                reviewCount = 134,
                price = 38.0,
                description = "Unique cat cafe with overnight stay",
                coordinates = Coordinates(10.7540, 106.6690),
                isPopular = true
            ),
            
            // Hotel
            Accommodation(
                id = "hotel_1",
                name = "Pet Hotel",
                category = AccommodationCategory.HOTEL,
                location = "Quận cam",
                district = "Quận cam",
                rating = 4.9f,
                reviewCount = 245,
                price = 80.0,
                description = "Premium pet hotel with excellent facilities",
                coordinates = Coordinates(10.8410, 106.8090),
                isPopular = true
            ),
            Accommodation(
                id = "hotel_2",
                name = "Grand Palace",
                category = AccommodationCategory.HOTEL,
                location = "Kuta",
                district = "Kuta",
                rating = 4.9f,
                reviewCount = 312,
                price = 105.0,
                description = "Luxury hotel for pets and owners",
                coordinates = Coordinates(10.8500, 106.8000),
                isPopular = false
            ),
            Accommodation(
                id = "hotel_3",
                name = "Masara Hotel",
                category = AccommodationCategory.HOTEL,
                location = "Sylhet",
                district = "Sylhet",
                rating = 4.9f,
                reviewCount = 189,
                price = 95.0,
                description = "Comfortable hotel with pet services",
                coordinates = Coordinates(10.8600, 106.8100),
                isPopular = false
            )
        )
    }
}

