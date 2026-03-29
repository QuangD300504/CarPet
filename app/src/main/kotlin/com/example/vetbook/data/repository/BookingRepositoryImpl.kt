package com.example.vetbook.data.repository

import android.app.Application
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.data.models.AppointmentDto
import com.example.vetbook.data.network.PayosApiService
import com.example.vetbook.data.network.PayosPaymentRequest
import com.example.vetbook.data.util.PayosHelper
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.models.PaymentLink
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.notification.ReminderNotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val payosApi: PayosApiService,
    private val application: Application
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
        notes: String?,
        petIds: List<String>
    ): BookingRepository.CreateAppointmentResult {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        // Fetch descriptive names before transaction for cleaner code
        val vetDoc = firestore.collection("veterinarians").document(veterinarianId).get().await()
        val vetName = vetDoc.getString("name") ?: "Unknown Doctor"
        val clinicId = vetDoc.getString("clinicId") ?: ""
        
        var clinicName = ""
        var clinicAddress = ""
        if (clinicId.isNotEmpty()) {
            val clinicDoc = firestore.collection("clinics").document(clinicId).get().await()
            clinicName = clinicDoc.getString("name") ?: ""
            clinicAddress = clinicDoc.getString("address") ?: ""
        }

        val petNames = petIds.map { pid ->
            val petDoc = firestore.collection("pets").document(pid).get().await()
            petDoc.getString("name") ?: "Unknown Pet"
        }

        val lockId = makeLockId(veterinarianId, appointmentAt)
        val lockRef = firestore.collection("doctorSlotLocks").document(lockId)
        val appointmentRef = firestore.collection("appointments").document()

        return firestore.runTransaction { tx ->
            val lockSnap = tx.get(lockRef)
            if (lockSnap.exists()) {
                android.util.Log.w("BookingRepo", "Lock already exists for $lockId")
                throw IllegalStateException("Time slot already booked")
            }
            android.util.Log.d("BookingRepo", "Creating lock and appointment: $lockId")

            tx.set(
                lockRef,
                mapOf(
                    "id" to lockId,
                    "veterinarianId" to veterinarianId,
                    "appointmentAt" to Timestamp(appointmentAt),
                    "createdAt" to Timestamp.now(),
                    "expiresAt" to Timestamp(Date(System.currentTimeMillis() + 15 * 60 * 1000))
                )
            )

            val now = Timestamp.now()
            val appointment = AppointmentDto(
                id = appointmentRef.id,
                userId = uid,
                veterinarianId = veterinarianId,
                veterinarianName = vetName,
                clinicName = clinicName,
                clinicAddress = clinicAddress,
                status = "PENDING_PAYMENT",
                paymentStatus = "UNPAID",
                appointmentAt = Timestamp(appointmentAt),
                durationMinutes = durationMinutes,
                notes = notes,
                petIds = petIds,
                petNames = petNames,
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

    }

    override suspend fun cancelAppointment(appointmentId: String, lockId: String) {
        android.util.Log.i("BookingRepo", "Cancelling appointment $appointmentId and removing lock $lockId")
        val lockRef = firestore.collection("doctorSlotLocks").document(lockId)
        val appointmentRef = firestore.collection("appointments").document(appointmentId)
        firestore.runTransaction { tx ->
            tx.delete(lockRef)
            tx.delete(appointmentRef)
        }.await()
        ReminderNotificationHelper.cancelAppointmentReminder(
            application,
            "appointment_reminder_$appointmentId"
        )
    }

    override suspend fun createPaymentLinkForAppointment(appointmentId: String): PaymentLink {
        val appointmentDoc = firestore.collection("appointments").document(appointmentId).get().await()
        if (!appointmentDoc.exists()) throw IllegalStateException("Appointment not found")
        
        val totalPrice = appointmentDoc.getDouble("totalPrice") ?: 0.0
        val orderCode = System.currentTimeMillis()
        val amount = totalPrice.toInt()
        val description = "VetBook appt ${appointmentId.take(5)}"
        val cancelUrl = "vetbook-payos://payment-result"
        val returnUrl = "vetbook-payos://payment-result"

        val params = mapOf(
            "amount" to amount,
            "cancelUrl" to cancelUrl,
            "description" to description,
            "orderCode" to orderCode,
            "returnUrl" to returnUrl
        )
        
        val signature = PayosHelper.calculateSignature(params)
        
        val request = PayosPaymentRequest(
            orderCode = orderCode,
            amount = amount,
            description = description,
            cancelUrl = cancelUrl,
            returnUrl = returnUrl,
            signature = signature
        )

        val response = payosApi.createPaymentLink(PayosHelper.CLIENT_ID, PayosHelper.API_KEY, request)
        
        if (response.code == null) {
    throw IllegalStateException("Lỗi kết nối PayOS. Vui lòng thử lại.")
}
if (response.code != "00") {
    throw IllegalStateException("PayOS: ${response.desc ?: "Lỗi không xác định"}")
}
val checkoutUrl = response.data?.checkoutUrl
    ?: throw IllegalStateException("PayOS không trả về link thanh toán.")

        // Save orderCode to appointment so we can keep track
        firestore.collection("appointments").document(appointmentId)
            .update("payos.orderCode", orderCode).await()

        return PaymentLink(
            checkoutUrl = checkoutUrl,
            orderCode = orderCode,
            paymentLinkId = appointmentId
        )
    }

    override fun getUserAppointments(userId: String): Flow<List<Appointment>> = callbackFlow {
        val subscription = firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            // NOTE: Removed orderBy("appointmentAt") — that requires a composite index.
            // Sorting is done in-memory below instead.
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointments = snapshot.toObjects(AppointmentDto::class.java)
                        .map { it.toDomain() }
                        .filter { it.status != "CANCELLED" } // hide cancelled from calendar
                        .sortedBy { it.appointmentAt } // sort in-memory
                    trySend(appointments)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getLockedSlots(
        veterinarianId: String,
        year: Int,
        month: Int,
        day: Int
    ): Set<String> {
        // Build UTC start/end of the given local calendar day
        val dayStart = Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val dayEnd = Calendar.getInstance().apply {
            set(year, month - 1, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val snapshot = firestore.collection("doctorSlotLocks")
            .whereEqualTo("veterinarianId", veterinarianId)
            .whereGreaterThanOrEqualTo("appointmentAt", com.google.firebase.Timestamp(dayStart))
            .whereLessThanOrEqualTo("appointmentAt", com.google.firebase.Timestamp(dayEnd))
            .get()
            .await()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        return snapshot.documents.mapNotNull { doc ->
            val ts = doc.getTimestamp("appointmentAt") ?: return@mapNotNull null
            timeFormat.format(ts.toDate())
        }.toSet()
    }

    override suspend fun markAppointmentAsPaid(appointmentId: String) {
        val appointmentRef = firestore.collection("appointments").document(appointmentId)

        // Read appointment data for the reminder
        val apptDoc = appointmentRef.get().await()
        val vetName = apptDoc.getString("veterinarianName") ?: ""
        val petNamesRaw = apptDoc.get("petNames") as? List<*>
        val petNames = petNamesRaw?.filterIsInstance<String>()?.joinToString(", ") ?: ""
        val appointmentAtTs = apptDoc.getTimestamp("appointmentAt")
        val appointmentAtDate = appointmentAtTs?.toDate()

        appointmentRef.update(
            "status", "UPCOMING",
            "paymentStatus", "PAID",
            "updatedAt", Timestamp.now(),
            "paidAt", Timestamp.now()
        ).await()

        // Schedule reminder: 24h before the appointment
        if (appointmentAtDate != null) {
            val reminderMillis = appointmentAtDate.time - (24 * 60 * 60 * 1000)
            val timeStr = appointmentAtDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
            ReminderNotificationHelper.scheduleAppointmentReminder(
                context = application,
                workName = "appointment_reminder_$appointmentId",
                vetName = vetName,
                petName = petNames,
                appointmentTime = timeStr,
                reminderTimeMillis = reminderMillis
            )
        }
    }

    override suspend fun markAppointmentCompleted(appointmentId: String) {
        val appointmentRef = firestore.collection("appointments").document(appointmentId)
        appointmentRef.update(
            "status", "COMPLETED",
            "updatedAt", Timestamp.now()
        ).await()
    }

    override suspend fun updateAppointmentNotes(appointmentId: String, notes: String) {
        firestore.collection("appointments").document(appointmentId)
            .update("notes", notes, "updatedAt", Timestamp.now())
            .await()
    }

    override fun getAllAppointments(): Flow<List<Appointment>> = callbackFlow {
        val subscription = firestore.collection("appointments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val all = snapshot.toObjects(AppointmentDto::class.java)
                        .map { it.toDomain() }
                        .sortedByDescending { it.appointmentAt } // sort in-memory
                    trySend(all)
                }
            }
        awaitClose { subscription.remove() }
    }
}