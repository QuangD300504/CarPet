package com.example.carpet.domain.usecases

import com.example.carpet.domain.models.Veterinarian
import com.example.carpet.domain.repository.VeterinarianRepository
import kotlinx.coroutines.flow.Flow

class GetVeterinariansUseCase(private val repository: VeterinarianRepository) {
    operator fun invoke(): Flow<List<Veterinarian>> = repository.getVeterinarians()
}
