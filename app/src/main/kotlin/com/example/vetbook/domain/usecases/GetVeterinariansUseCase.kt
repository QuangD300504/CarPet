package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.Veterinarian
import com.example.vetbook.domain.repository.VeterinarianRepository
import kotlinx.coroutines.flow.Flow

class GetVeterinariansUseCase(private val repository: VeterinarianRepository) {
    operator fun invoke(): Flow<List<Veterinarian>> = repository.getVeterinarians()
}
