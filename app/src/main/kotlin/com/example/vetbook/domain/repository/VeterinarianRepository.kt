package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.data.models.VeterinarianDto
import kotlinx.coroutines.flow.Flow

interface VeterinarianRepository {
    fun getVeterinarians(): Flow<List<Veterinarian>>
    suspend fun getVeterinarianDtoById(id: String): VeterinarianDto?
    suspend fun createVeterinarian(veterinarian: VeterinarianDto): Result<VeterinarianDto>
    suspend fun updateVeterinarian(id: String, fields: Map<String, Any?>)
    suspend fun deleteVeterinarian(id: String)
}
