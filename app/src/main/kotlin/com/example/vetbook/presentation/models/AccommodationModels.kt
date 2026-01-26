package com.example.vetbook.presentation.models

data class Accommodation(
    val id: String,
    val name: String,
    val category: AccommodationCategory,
    val location: String,
    val district: String,
    val rating: Float,
    val reviewCount: Int,
    val price: Double,
    val priceUnit: String = "USD",
    val imageUrl: String? = null,
    val description: String,
    val coordinates: Coordinates? = null,
    val isPopular: Boolean = false
)

enum class AccommodationCategory(val displayName: String, val icon: String) {
    HOMESTAY("Homestay", "home"),
    APART("Apart", "apartment"),
    COFFEE("Coffee", "coffee"),
    HOTEL("Hotel", "hotel")
}

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class AccommodationUiState(
    val accommodations: List<Accommodation> = emptyList(),
    val filteredAccommodations: List<Accommodation> = emptyList(),
    val selectedCategory: AccommodationCategory? = null,
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.LIST,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ViewMode {
    LIST, MAP
}

