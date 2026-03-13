package com.example.vetbook.domain.models

import java.time.Instant

data class Appointment(
    val id: String,
    val userId: String,
    val veterinarianId: String,
    val veterinarianName: String = "",
    val clinicName: String = "",
    val clinicAddress: String = "",
    val status: String,
    val paymentStatus: String,
    val appointmentAt: Instant,
    val durationMinutes: Int,
    val notes: String?,
    val petIds: List<String>,
    val petNames: List<String> = emptyList(),
    val totalPrice: Double
)
