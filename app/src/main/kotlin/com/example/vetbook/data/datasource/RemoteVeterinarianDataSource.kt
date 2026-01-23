package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.VeterinarianDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote source for veterinarian data.
 */
interface RemoteVeterinarianDataSource {

    fun observeVeterinarians(): Flow<List<VeterinarianDto>>
}


