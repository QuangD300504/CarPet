package com.example.vetbook.data.repository

import com.example.vetbook.R
import com.example.vetbook.domain.models.PetServiceDetail
import com.example.vetbook.domain.models.ServiceCategory
import com.example.vetbook.domain.models.ServicePackage
import com.example.vetbook.domain.repository.ServiceRepository

class MockServiceRepository : ServiceRepository {
    override fun getCategories(): List<ServiceCategory> {
        return listOf(
            ServiceCategory(
                id = "cat_vet",
                title = "Vet care",
                shortDescription = "24/7 smart booking with verified clinics",
                iconRes = R.drawable.checkup
            ),
            ServiceCategory(
                id = "cat_hotel",
                title = "Stay & Care",
                shortDescription = "Safe and comfortable stay for your pets",
                iconRes = R.drawable.hotel
            ),
            ServiceCategory(
                id = "cat_ride",
                title = "Pet Ride",
                shortDescription = "Safe transportation for your pets",
                iconRes = R.drawable.homecare // Using homecare icon as placeholder
            ),
            ServiceCategory(
                id = "cat_spa",
                title = "Pamper",
                shortDescription = "Deep cleaning and styling by certified experts",
                iconRes = R.drawable.groom
            ),
            ServiceCategory(
                id = "cat_training",
                title = "Training",
                shortDescription = "Professional pet training services",
                iconRes = R.drawable.checkup // Using checkup icon as placeholder
            ),
            ServiceCategory(
                id = "cat_party",
                title = "Party",
                shortDescription = "Pet party and celebration services",
                iconRes = R.drawable.hotel // Using hotel icon as placeholder
            ),
            ServiceCategory(
                id = "cat_funeral",
                title = "Funeral",
                shortDescription = "Pet funeral and memorial services",
                iconRes = R.drawable.groom // Using groom icon as placeholder
            )
        )
    }

    override fun getServiceDetail(categoryId: String): PetServiceDetail? {
        val allDetails = listOf(
            PetServiceDetail(
                categoryId = "cat_vet",
                rating = 4.8f,
                reviewCount = "1.6k reviews",
                about = "Professional pet grooming services including bathing, hair cutting, nail trimming, and more. Our experienced groomers ensure your pet looks and feels their best.",
                packages = listOf(
                    ServicePackage("v1", "General Check-up", 25.0),
                    ServicePackage("v2", "Vaccination", 50.0),
                    ServicePackage("v3", "Deworming & Parasite Control", 15.0),
                    ServicePackage("v4", "Dental Care", 30.0)
                ),
                availableTimes = listOf("9:00 AM", "11:00 AM", "2:00 PM"),
                bannerGradientColors = listOf(0xFFD1C4E9, 0xFFBBDEFB)
            ),

            PetServiceDetail(
                categoryId = "cat_spa",
                rating = 4.3f,
                reviewCount = "1.2k reviews",
                about = "Professional pet grooming services including bathing, hair cutting, nail trimming, and more. Our experienced groomers ensure your pet looks and feels their best.",
                packages = listOf(
                    ServicePackage("s1", "Basic Bath", 25.0),
                    ServicePackage("s2", "Full Grooming", 50.0),
                    ServicePackage("s3", "Nail Trim", 15.0),
                    ServicePackage("s4", "Teeth Cleaning", 30.0)
                ),
                availableTimes = listOf("9:00 AM", "11:00 AM", "2:00 PM"),
                bannerGradientColors = listOf(0xFFFFE0B2, 0xFFDCEDC8)
            ),

            PetServiceDetail(
                categoryId = "cat_hotel",
                rating = 4.5f,
                reviewCount = "1.1k reviews",
                about = "Professional pet grooming services including bathing, hair cutting, nail trimming, and more. Our experienced groomers ensure your pet looks and feels their best.",
                packages = listOf(
                    ServicePackage("h1", "Basic Bath", 25.0),
                    ServicePackage("h2", "Full Grooming", 50.0),
                    ServicePackage("h3", "Nail Trim", 15.0),
                    ServicePackage("h4", "Teeth Cleaning", 30.0)
                ),
                availableTimes = listOf("9:00 AM", "11:00 AM", "2:00 PM"),
                bannerGradientColors = listOf(0xFFE1F5FE, 0xFFE3F2FD)
            ),

            PetServiceDetail(
                categoryId = "cat_homecare",
                rating = 4.0f,
                reviewCount = "500 reviews",
                about = "Professional pet grooming services including bathing, hair cutting, nail trimming, and more. Our experienced groomers ensure your pet looks and feels their best.",
                packages = listOf(
                    ServicePackage("hc1", "Basic Bath", 25.0),
                    ServicePackage("hc2", "Full Grooming", 50.0),
                    ServicePackage("hc3", "Nail Trim", 15.0),
                    ServicePackage("hc4", "Teeth Cleaning", 30.0)
                ),
                availableTimes = listOf("9:00 AM", "11:00 AM", "2:00 PM"),
                bannerGradientColors = listOf(0xFFE0F2F1, 0xFFFFF9C4)
            )
        )
        return allDetails.find { it.categoryId == categoryId }
    }
}