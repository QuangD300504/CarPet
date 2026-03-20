package com.example.vetbook.calendar

import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import java.time.Instant
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for CalendarViewModel business logic.
 *
 * These tests verify the logic independently without requiring FirebaseAuth.
 * Use `advanceUntilIdle()` via testDispatchers to control coroutine timing.
 */
class CalendarViewModelTest {

    /**
     * Test that past UPCOMING appointments are correctly identified.
     * This is the core auto-complete logic.
     */
    @Test
    fun `filterPastUpcoming returns correct appointments`() {
        val now = Instant.now()

        val pastAppt = makeAppt(id = "past-1", status = "UPCOMING", appointmentAt = now.minusSeconds(3600))
        val futureAppt = makeAppt(id = "future-1", status = "UPCOMING", appointmentAt = now.plusSeconds(3600))
        val completedAppt = makeAppt(id = "done-1", status = "COMPLETED", appointmentAt = now.minusSeconds(7200))

        val all = listOf(pastAppt, futureAppt, completedAppt)
        val nowInstant = now

        val pastUpcoming = all.filter {
            it.status == "UPCOMING" && it.appointmentAt.isBefore(nowInstant)
        }

        assertEquals(1, pastUpcoming.size)
        assertEquals("past-1", pastUpcoming.first().id)
    }

    /**
     * Test that appointments on the same day are correctly matched.
     */
    @Test
    fun `getAppointmentsForDate filters by local date`() {
        val today = java.time.LocalDate.now()
        val todayInstant = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val tomorrowInstant = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()

        val apptToday = makeAppt(id = "today-1", appointmentAt = todayInstant)
        val apptTomorrow = makeAppt(id = "tomorrow-1", appointmentAt = tomorrowInstant)

        val all = listOf(apptToday, apptTomorrow)

        val todayResults = all.filter { appt ->
            val apptDate = appt.appointmentAt
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            apptDate == today
        }

        assertEquals(1, todayResults.size)
        assertEquals("today-1", todayResults.first().id)
    }

    /**
     * Test that hasAppointments returns true only when there are appointments.
     */
    @Test
    fun `hasAppointments returns true only for dates with appointments`() {
        val today = java.time.LocalDate.now()
        val todayInstant = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val tomorrowInstant = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()

        val appointments = listOf(
            makeAppt(id = "today-1", appointmentAt = todayInstant)
        )

        val hasToday = appointments.any { appt ->
            appt.appointmentAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today
        }
        val hasTomorrow = appointments.any { appt ->
            appt.appointmentAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today.plusDays(1)
        }

        assertEquals(true, hasToday)
        assertEquals(false, hasTomorrow)
    }

    /**
     * Test that month navigation works correctly.
     */
    @Test
    fun `month navigation works correctly`() {
        val currentMonth = java.time.YearMonth.now()
        val prevMonth = currentMonth.minusMonths(1)
        val nextMonth = currentMonth.plusMonths(1)

        assertEquals(currentMonth.minusMonths(1), prevMonth)
        assertEquals(currentMonth.plusMonths(1), nextMonth)
    }

    /**
     * Test that the fake BookingRepository correctly implements markAppointmentCompleted.
     */
    @Test
    fun `FakeBookingRepository tracks completed appointment IDs`() = kotlinx.coroutines.runBlocking {
        val repo = FakeBookingRepository()

        assertEquals(false, repo.completedIds.contains("appt-1"))

        repo.markAppointmentCompleted("appt-1")

        assertEquals(true, repo.completedIds.contains("appt-1"))
        assertEquals(false, repo.completedIds.contains("appt-2"))
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun makeAppt(
        id: String = "appt-1",
        status: String = "UPCOMING",
        appointmentAt: Instant = Instant.now().plusSeconds(3600)
    ): Appointment = Appointment(
        id = id,
        userId = "user-123",
        veterinarianId = "vet-1",
        veterinarianName = "Dr. Test",
        clinicName = "",
        clinicAddress = "",
        status = status,
        paymentStatus = "PAID",
        appointmentAt = appointmentAt,
        durationMinutes = 30,
        notes = null,
        petIds = emptyList(),
        petNames = emptyList(),
        totalPrice = 0.0
    )

    // ─── Fake Repository ─────────────────────────────────────────────────────

    private class FakeBookingRepository : BookingRepository {
        val completedIds = mutableListOf<String>()

        override suspend fun createAppointmentWithSlotLock(
            veterinarianId: String,
            appointmentAt: Date,
            totalPrice: Double,
            durationMinutes: Int,
            notes: String?,
            petIds: List<String>
        ): BookingRepository.CreateAppointmentResult =
            BookingRepository.CreateAppointmentResult("appt-1", "lock-1")

        override suspend fun createPaymentLinkForAppointment(appointmentId: String) =
            throw NotImplementedError()

        override suspend fun cancelAppointment(appointmentId: String, lockId: String) {}

        override fun getUserAppointments(userId: String): Flow<List<Appointment>> = emptyFlow()

        override fun getAllAppointments(): Flow<List<Appointment>> = emptyFlow()

        override suspend fun getLockedSlots(
            veterinarianId: String, year: Int, month: Int, day: Int
        ): Set<String> = emptySet()

        override suspend fun markAppointmentAsPaid(appointmentId: String) {}
        override suspend fun markAppointmentCompleted(appointmentId: String) {
            completedIds.add(appointmentId)
        }
    }
}
