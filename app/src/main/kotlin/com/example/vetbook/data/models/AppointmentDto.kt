package com.example.vetbook.data.models

import com.google.firebase.Timestamp

data class AppointmentDto(
    val id: String = "",
    val userId: String = "",
    val veterinarianId: String = "",
    val status: String = "PENDING_PAYMENT",
    val paymentStatus: String = "UNPAID",
    val appointmentAt: Timestamp = Timestamp.now(),
    val durationMinutes: Int = 30,
    val notes: String? = null,
    val totalPrice: Double = 0.0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val paidAt: Timestamp? = null,
    val payos: Payos? = null
) {
    data class Payos(
        val orderCode: Long? = null,
        val paymentLinkId: String? = null,
        val checkoutUrl: String? = null
    )
}
