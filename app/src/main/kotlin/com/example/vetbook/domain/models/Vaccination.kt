package com.example.vetbook.domain.models

import java.time.Instant

/**
 * Represents a vaccination record for a pet.
 * Enhanced version with status tracking and reminders.
 */
data class Vaccination(
    val id: String,
    val petId: String,
    val veterinarianId: String? = null,
    val veterinarianName: String? = null,
    val clinicName: String? = null,
    
    // Vaccination details
    val title: String,
    val type: VaccinationType = VaccinationType.CORE,
    val manufacturer: String? = null,
    val batchNumber: String? = null,
    
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
    CORE,      // Essential vaccinations (Rabies, Distemper)
    NON_CORE,  // Recommended based on lifestyle
    OPTIONAL   // Based on risk factors
}

enum class VaccinationStatus {
    SCHEDULED,  // Future vaccination
    COMPLETED,  // Done
    OVERDUE,    // Missed the due date
    SKIPPED     // User chose not to do it
}