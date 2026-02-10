package com.example.vetbook.data.mappers

import com.example.vetbook.data.models.AppointmentDto
import com.example.vetbook.domain.models.Appointment

fun AppointmentDto.toDomain(): Appointment {
    return Appointment(
        id = id,
        userId = userId,
        veterinarianId = veterinarianId,
        status = status,
        paymentStatus = paymentStatus,
        appointmentAt = appointmentAt.toDate().toInstant(),
        durationMinutes = durationMinutes,
        notes = notes,
        totalPrice = totalPrice
    )
}
