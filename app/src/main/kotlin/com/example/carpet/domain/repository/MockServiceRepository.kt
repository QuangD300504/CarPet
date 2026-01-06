package com.example.carpet.data.repository

import com.example.carpet.R
import com.example.carpet.domain.models.ServiceCategory
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
            ),
            ServiceCategory(
                id = "cat_hotel",
                title = "Pet Hotel & Boarding",
                shortDescription = "Safe and comfortable stay for your pets",
                iconRes = R.drawable.hotel
            )
        )
    }
}