package com.example.vetbook.data.repository

import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.domain.repository.VeterinarianRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockVeterinarianRepository : VeterinarianRepository {
    override fun getVeterinarians(): Flow<List<Veterinarian>> = flowOf(
        listOf(
            Veterinarian(
                id = "1", 
                name = "Dr. Sarah Johnson", 
                specialty = "Small Animal Medicine", 
                experience = "12 years experience", 
                initials = "DSJ",
                bio = "Compassionate veterinarian specializing in the comprehensive care of small household pets.",
                reviewsCount = 120
            ),
            Veterinarian(
                id = "2", 
                name = "Dr. Michael Chen", 
                specialty = "Surgery & Emergency Care", 
                experience = "15 years experience", 
                initials = "DMC",
                bio = "Highly skilled surgeon dedicated to providing emergency medical services for critical pet cases.",
                reviewsCount = 85
            ),
            Veterinarian(
                id = "3", 
                name = "Trương Tuấn Tú", 
                specialty = "Exotic Animals", 
                experience = "36 years experience", 
                initials = "DER",
                bio = "Expert in treating birds, reptiles, and other exotic pets with gentle and specialized care.",
                reviewsCount = 36
            ),
            Veterinarian(
                id = "4", 
                name = "Dr. David Thompson", 
                specialty = "Dental Care", 
                experience = "10 years experience", 
                initials = "DDT",
                bio = "Specialist in veterinary dentistry, focusing on maintaining optimal oral health for your furry companions.",
                reviewsCount = 50
            )
        )
    )
}
