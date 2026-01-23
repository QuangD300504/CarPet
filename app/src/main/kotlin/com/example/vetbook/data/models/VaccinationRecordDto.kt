package com.example.vetbook.data.models

/**
 * Document in `pets/{petId}/vaccinations`.
 */
data class VaccinationRecordDto(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val date: Long? = null,
    val veterinarianId: String? = null,
    val notes: String? = null,
    val createdAt: Long = 0L
)


