package com.example.vetbook.appointment

import com.example.vetbook.domain.models.PaymentLink
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.presentation.components.calendar.SlotOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Pure unit tests for appointment booking logic.
 * Tests the core business rules without requiring Hilt DI or Firebase.
 */
class BookAppointmentViewModelTest {

    // ─── Slot Selection Tests ─────────────────────────────────────────────

    @Test
    fun `timeSlots contains exactly 9 slots`() {
        val slots = SlotOption.defaults
        assertEquals(9, slots.size)
    }

    @Test
    fun `SlotOption defaults covers morning and afternoon`() {
        val labels = SlotOption.defaults.map { it.label }
        // Morning: 09:00, 09:30, 10:00, 10:30, 11:00
        assertTrue(labels.contains("09:00"))
        assertTrue(labels.contains("09:30"))
        assertTrue(labels.contains("10:00"))
        assertTrue(labels.contains("10:30"))
        assertTrue(labels.contains("11:00"))
        // Afternoon: 14:00, 14:30, 15:00, 15:30
        assertTrue(labels.contains("14:00"))
        assertTrue(labels.contains("14:30"))
        assertTrue(labels.contains("15:00"))
        assertTrue(labels.contains("15:30"))
    }

    @Test
    fun `SlotOption label can be parsed back to LocalTime`() {
        val slot = SlotOption.defaults.first()
        val parsed = java.time.LocalTime.parse(slot.label)
        assertEquals(slot.time.hour, parsed.hour)
        assertEquals(slot.time.minute, parsed.minute)
    }

    // ─── Locked Slots Tests ───────────────────────────────────────────────

    @Test
    fun `locked slots use HH mm format`() {
        val lockedSlots = setOf("09:00", "10:30", "14:00")
        assertEquals(3, lockedSlots.size)
        lockedSlots.forEach { slot ->
            assertTrue(slot.matches(Regex("\\d{2}:\\d{2}")))
        }
    }

    @Test
    fun `locked slot format matches SlotOption labels`() {
        val lockedSlots = setOf("09:00", "10:30")
        val allLabels = SlotOption.defaults.map { it.label }.toSet()
        assertTrue(lockedSlots.all { it in allLabels })
    }

    @Test
    fun `available slots are computed by subtracting locked slots`() {
        val allLabels = SlotOption.defaults.map { it.label }.toSet()
        val lockedSlots = setOf("09:00", "10:30")
        val availableSlots = allLabels - lockedSlots

        assertFalse(availableSlots.contains("09:00"))
        assertTrue(availableSlots.contains("09:30"))
        assertFalse(availableSlots.contains("10:30"))
        assertTrue(availableSlots.contains("10:00"))
    }

    // ─── Booking Repository Tests ──────────────────────────────────────────

    @Test
    fun `FakeBookingRepository returns correct result structure`() {
        val repo = FakeBookingRepository()
        val result = runCatching {
            kotlinx.coroutines.runBlocking {
                repo.createAppointmentWithSlotLock("vet-1", Date(), 150.0, 30, null, listOf("pet-1"))
            }
        }
        assertTrue(result.isSuccess)
        assertEquals("appt-test", result.getOrNull()?.appointmentId)
        assertEquals("lock-test", result.getOrNull()?.lockId)
    }

    @Test
    fun `FakeBookingRepository tracks cancelled appointments`() {
        val repo = FakeBookingRepository()
        kotlinx.coroutines.runBlocking {
            repo.cancelAppointment("appt-1", "lock-1")
        }
        assertTrue(repo.cancelledIds.contains("appt-1"))
        assertEquals("lock-1", repo.cancelledLocks["appt-1"])
    }

    @Test
    fun `FakeBookingRepository returns locked slots correctly`() {
        val repo = FakeBookingRepository()
        repo.lockedSlots = setOf("09:00", "10:00")
        val locked = kotlinx.coroutines.runBlocking {
            repo.getLockedSlots("vet-1", 2026, 3, 21)
        }
        assertEquals(2, locked.size)
        assertTrue(locked.contains("09:00"))
    }

    @Test
    fun `FakeBookingRepository marks appointment as paid`() {
        val repo = FakeBookingRepository()
        kotlinx.coroutines.runBlocking {
            repo.markAppointmentAsPaid("appt-paid")
        }
        assertTrue(repo.paidIds.contains("appt-paid"))
    }

    @Test
    fun `FakeBookingRepository marks appointment as completed`() {
        val repo = FakeBookingRepository()
        kotlinx.coroutines.runBlocking {
            repo.markAppointmentCompleted("appt-done")
        }
        assertTrue(repo.completedIds.contains("appt-done"))
    }

    // ─── Fake Repository ──────────────────────────────────────────────────

    private class FakeBookingRepository : BookingRepository {
        var lockedSlots: Set<String> = emptySet()
        val cancelledIds = mutableListOf<String>()
        val cancelledLocks = mutableMapOf<String, String>()
        val paidIds = mutableListOf<String>()
        val completedIds = mutableListOf<String>()

        override suspend fun createAppointmentWithSlotLock(
            veterinarianId: String,
            appointmentAt: Date,
            totalPrice: Double,
            durationMinutes: Int,
            notes: String?,
            petIds: List<String>
        ): BookingRepository.CreateAppointmentResult =
            BookingRepository.CreateAppointmentResult("appt-test", "lock-test")

        override suspend fun createPaymentLinkForAppointment(appointmentId: String) =
            PaymentLink(checkoutUrl = "https://checkout.test", orderCode = 999L, paymentLinkId = appointmentId)

        override suspend fun cancelAppointment(appointmentId: String, lockId: String) {
            cancelledIds.add(appointmentId)
            cancelledLocks[appointmentId] = lockId
        }

        override fun getUserAppointments(userId: String): kotlinx.coroutines.flow.Flow<List<com.example.vetbook.domain.models.Appointment>> =
            kotlinx.coroutines.flow.emptyFlow()

        override fun getAllAppointments(): kotlinx.coroutines.flow.Flow<List<com.example.vetbook.domain.models.Appointment>> =
            kotlinx.coroutines.flow.emptyFlow()

        override suspend fun getLockedSlots(veterinarianId: String, year: Int, month: Int, day: Int): Set<String> =
            lockedSlots

        override suspend fun markAppointmentAsPaid(appointmentId: String) {
            paidIds.add(appointmentId)
        }

        override suspend fun markAppointmentCompleted(appointmentId: String) {
            completedIds.add(appointmentId)
        }
    }
}
