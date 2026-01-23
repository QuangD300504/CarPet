package com.example.vetbook.data.models

data class AppointmentDto(
    val id: String = "",
    val userId: String = "",
    val veterinarianId: String? = null,
    val serviceId: String? = null,
    val petId: String = "",
    val packageId: String? = null,
    val status: String = "pending",
    val appointmentAt: Long = 0L,
    val durationMinutes: Int = 0,
    val notes: String? = null,
    val totalPrice: Double = 0.0,
    val paymentStatus: String = "pending",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null
)
