package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.ClinicDto
import kotlinx.coroutines.flow.Flow

interface RemoteClinicDataSource {
    fun observeClinics(): Flow<List<ClinicDto>>
    suspend fun getClinicById(id: String): ClinicDto?
    suspend fun createClinic(clinic: ClinicDto): Result<ClinicDto>
    suspend fun updateClinic(id: String, fields: Map<String, Any?>)
}
