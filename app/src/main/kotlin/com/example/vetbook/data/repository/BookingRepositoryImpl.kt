package com.example.vetbook.data.repository

import com.example.vetbook.data.models.AppointmentDto
import com.example.vetbook.data.network.PayosWorkerApi
import com.example.vetbook.domain.models.PaymentLink
import com.example.vetbook.domain.repository.BookingRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val payosWorkerApi: PayosWorkerApi
) : BookingRepository {

    private fun makeLockId(veterinarianId: String, appointmentAt: Date): String {
        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(appointmentAt)
        val timeKey = SimpleDateFormat("HHmm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(appointmentAt)
        return "${veterinarianId}_${dateKey}_${timeKey}"
    }

    override suspend fun createAppointmentWithSlotLock(
        veterinarianId: String,
        appointmentAt: Date,
        totalPrice: Double,
        durationMinutes: Int,
        notes: String?
    ): BookingRepository.CreateAppointmentResult {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        val lockId = makeLockId(veterinarianId, appointmentAt)
        val lockRef = firestore.collection("doctorSlotLocks").document(lockId)
        val appointmentRef = firestore.collection("appointments").document()

        firestore.runTransaction { tx ->
            val lockSnap = tx.get(lockRef)
            if (lockSnap.exists()) {
                throw IllegalStateException("Time slot already booked")
            }

            tx.set(
                lockRef,
                mapOf(
                    "id" to lockId,
                    "veterinarianId" to veterinarianId,
                    "appointmentAt" to Timestamp(appointmentAt),
                    "createdAt" to Timestamp.now()
                )
            )

            val now = Timestamp.now()
            val appointment = AppointmentDto(
                id = appointmentRef.id,
                userId = uid,
                veterinarianId = veterinarianId,
                status = "PENDING_PAYMENT",
                paymentStatus = "UNPAID",
                appointmentAt = Timestamp(appointmentAt),
                durationMinutes = durationMinutes,
                notes = notes,
                totalPrice = totalPrice,
                createdAt = now,
                updatedAt = now,
                paidAt = null,
                payos = null
            )

            tx.set(appointmentRef, appointment)

            BookingRepository.CreateAppointmentResult(
                appointmentId = appointmentRef.id,
                lockId = lockId
            )
        }.await()

        return BookingRepository.CreateAppointmentResult(appointmentId = appointmentRef.id, lockId = lockId)
    }

    override suspend fun createPayosPaymentLink(appointmentId: String): PaymentLink {
        val token = auth.currentUser?.getIdToken(false)?.await()?.token ?: error("Missing ID token")
        val resp = payosWorkerApi.createPaymentLink(
            authorization = "Bearer $token",
            body = PayosWorkerApi.CreatePaymentLinkRequest(appointmentId)
        )
        return PaymentLink(
            checkoutUrl = resp.checkoutUrl,
            orderCode = resp.orderCode,
            paymentLinkId = resp.paymentLinkId
        )
    }
}
