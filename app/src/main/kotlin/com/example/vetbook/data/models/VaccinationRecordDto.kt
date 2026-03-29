package com.example.vetbook.data.models

import androidx.annotation.Keep

@Keep
data class VaccinationRecordDto(
    val id: String = "",
    val petId: String = "",
    /** Owner userId — needed for push notifications */
    val ownerId: String = "",
    /** Pet display name — used in push message body */
    val petName: String = "",
    val veterinarianId: String? = null,
    val veterinarianName: String? = null,
    val clinicName: String? = null,

    // Vaccination details
    val title: String = "",
    val type: String = "CORE",
    /** Alternate / brand name */
    val alsoKnownAs: String? = null,
    val manufacturer: String? = null,
    val batchNumber: String? = null,

    // Schedule metadata (from WSAVA template)
    val offsetDays: Int? = null,
    val isRecurring: Boolean = false,
    val intervalDays: Int? = null,
    val lifestyleTrigger: String? = null,

    // Status & Dates (milliseconds)
    val status: String = "SCHEDULED",
    val scheduledDate: Long? = null,
    val completedDate: Long? = null,
    val nextDueDate: Long? = null,

    // FIX: persists the appointment booking linked to this vaccination record.
    // Was missing from DTO, causing linkAppointment() to silently drop the value
    // on every Firestore write via toDto().
    val linkedBookingId: String? = null,

    // Documentation
    val certificateUrl: String? = null,
    val notes: String? = null,
    val sideEffects: String? = null,

    // Metadata
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,

    // Reminder
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 7,

    // Legacy fields (backward compatibility)
    @Deprecated("Use status instead")
    val isCompleted: Boolean = false,
    @Deprecated("Use scheduledDate or completedDate")
    val date: Long? = null
)