package com.example.carpet.domain.repository

import com.example.carpet.domain.models.Veterinarian
import kotlinx.coroutines.flow.Flow

interface VeterinarianRepository {
    fun getVeterinarians(): Flow<List<Veterinarian>>
}
