package com.example.carpet.data.repository

import com.example.carpet.R
import com.example.carpet.domain.models.PetServiceDetail
import com.example.carpet.domain.models.ServiceCategory
import com.example.carpet.domain.models.ServicePackage
import com.example.carpet.domain.repository.ServiceRepository

class MockServiceRepository : ServiceRepository {
    override fun getCategories(): List<ServiceCategory> {
        return listOf(
            ServiceCategory(
                id = "cat_vet",
                title = "Veterinary & Health",
                shortDescription = "24/7 smart booking with verified clinics",
                iconRes = R.drawable.checkup
            ),
            ServiceCategory(
                id = "cat_spa",
                title = "Professional Grooming",
                shortDescription = "Deep cleaning and styling by certified experts",
                iconRes = R.drawable.groom
            ),ServiceCategory(
                id = "cat_homecare",
                title = "Home Care Service",
                shortDescription = "Pet sitter visits your home",
                iconRes = R.drawable.homecare
            ),ServiceCategory(
                id = "cat_homecare",
                title = "Skibidi",
                shortDescription = "Pet sitter visits your home",
                iconRes = R.drawable.homecare
            ),
            ServiceCategory(
                id = "cat_hotel",
                title = "Pet Hotel & Boarding",
                shortDescription = "Safe and comfortable stay for your pets",
                iconRes = R.drawable.hotel
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