package com.example.vetbook.domain.repository

import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.models.PaymentLink
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface BookingRepository {
    data class CreateAppointmentResult(
        val appointmentId: String,
        val lockId: String
    )

    suspend fun createAppointmentWithSlotLock(
        veterinarianId: String,
        appointmentAt: Date,
        totalPrice: Double,
        durationMinutes: Int = 30,
        notes: String? = null
    ): CreateAppointmentResult

    suspend fun createPaymentLinkForAppointment(appointmentId: String): PaymentLink

    fun getUserAppointments(userId: String): Flow<List<Appointment>>
}
