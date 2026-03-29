package com.example.vetbook.data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
@Keep
data class AppointmentDto(
    val id: String = "",
    val userId: String = "",
    val veterinarianId: String = "",
    val veterinarianName: String = "",
    val clinicName: String = "",
    val clinicAddress: String = "",
    val status: String = "PENDING_PAYMENT",
    val paymentStatus: String = "UNPAID",
    val appointmentAt: Timestamp = Timestamp.now(),
    val durationMinutes: Int = 30,
    val notes: String? = null,
    val petIds: List<String> = emptyList(),
    val petNames: List<String> = emptyList(),
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
