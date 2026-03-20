package com.example.vetbook.data.models

data class VaccinationRecordDto(
    val id: String = "",
    val petId: String = "",
    val veterinarianId: String? = null,
    val veterinarianName: String? = null,
    val clinicName: String? = null,
    
    // Vaccination details
    val title: String = "",
    val type: String = "CORE",
    val manufacturer: String? = null,
    val batchNumber: String? = null,
    
    // Status & Dates (milliseconds)
    val status: String = "SCHEDULED",
    val scheduledDate: Long? = null,
    val completedDate: Long? = null,
    val nextDueDate: Long? = null,
    
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