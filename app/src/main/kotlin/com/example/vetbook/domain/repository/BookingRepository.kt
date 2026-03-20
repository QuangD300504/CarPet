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
        notes: String? = null,
        petIds: List<String> = emptyList()
    ): CreateAppointmentResult

    suspend fun createPaymentLinkForAppointment(appointmentId: String): PaymentLink

    suspend fun cancelAppointment(appointmentId: String, lockId: String)

    fun getUserAppointments(userId: String): Flow<List<Appointment>>

    /** Admin only — observe every appointment across all users */
    fun getAllAppointments(): Flow<List<Appointment>>

    /**
     * Returns the set of time strings (e.g. "09:00") that are already locked
     * for the given veterinarian on the given date (local date components).
     */
    suspend fun getLockedSlots(veterinarianId: String, year: Int, month: Int, day: Int): Set<String>
    suspend fun markAppointmentAsPaid(appointmentId: String)

    suspend fun markAppointmentCompleted(appointmentId: String)
}
