package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Veterinarian
import kotlinx.coroutines.flow.Flow

interface VeterinarianRepository {
    fun getVeterinarians(): Flow<List<Veterinarian>>
}
