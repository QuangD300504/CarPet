package com.example.vetbook.data.models

/**
 * Document in `petEvents/{eventId}/participants` subcollection.
 */
data class EventParticipantDto(
    val userId: String = "",
    val joinedAt: Long = 0L
)


