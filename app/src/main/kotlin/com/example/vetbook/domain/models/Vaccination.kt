package com.example.vetbook.domain.models

import java.time.Instant

/**
 * Represents a vaccination record for a pet.
 * Enhanced version with status tracking and reminders.
 */
data class Vaccination(
    val id: String,
    val petId: String,
    /** Owner userId — needed for push notifications */
    val ownerId: String = "",
    /** Pet display name — used in push message body */
    val petName: String = "",
    val veterinarianId: String? = null,
    val veterinarianName: String? = null,
    val clinicName: String? = null,

    // Vaccination details
    val title: String,
    val type: VaccinationType = VaccinationType.CORE,
    val linkedBookingId: String? = null,
    /** Alternate / brand name, e.g. "Distemper combo" for DHPP */
    val alsoKnownAs: String? = null,
    val manufacturer: String? = null,
    val batchNumber: String? = null,

    // Schedule metadata (from WSAVA template)
    /** Days after birth when this dose is due */
    val offsetDays: Int? = null,
    /** Whether this vaccine requires periodic boosters */
    val isRecurring: Boolean = false,
    /** Days between booster doses (only meaningful when isRecurring=true) */
    val intervalDays: Int? = null,
    /** Why this lifestyle vaccine is recommended for this pet */
    val lifestyleTrigger: String? = null,
    val description: String? = null,

    // Status & Dates
    val status: VaccinationStatus,
    val scheduledDate: Instant? = null,
    val completedDate: Instant? = null,
    val nextDueDate: Instant? = null, // For boosters

    // Documentation
    val certificateUrl: String? = null, // PDF/Image of certificate
    val notes: String? = null,
    val sideEffects: String? = null,

    // Metadata
    val createdAt: Instant,
    val updatedAt: Instant = Instant.now(),

    // Reminder
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 7
)

enum class VaccinationType {
    CORE,            // Essential for all pets (WSAVA-mandated)
    REGIONAL,        // Endemic-area core — recommended in specific regions
    LIFESTYLE,       // Risk-based — depends on pet's activities/environment
    NOT_RECOMMENDED, // WSAVA-flagged — insufficient clinical evidence
    CUSTOM           // User-defined vaccine
}

enum class VaccinationStatus {
    PENDING,    // Selected, no appointment booked yet
    SCHEDULED,  // Appointment booked, date confirmed
    COMPLETED,  // Injected
    OVERDUE,    // Appointment passed, not marked done
    SKIPPED
}