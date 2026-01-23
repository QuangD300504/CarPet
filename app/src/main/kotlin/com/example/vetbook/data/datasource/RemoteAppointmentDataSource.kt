package com.example.vetbook.data.datasource

import com.example.vetbook.data.models.AppointmentDto

/**
 * Remote source for appointment-related operations.
 *
 * Even if appointments are not yet exposed in the UI, having this
 * abstraction in place makes it easy to plug the feature in later.
 */
interface RemoteAppointmentDataSource {

    suspend fun getUserAppointments(userId: String): List<AppointmentDto>

    suspend fun getVeterinarianAppointments(vetId: String): List<AppointmentDto>

    suspend fun createOrUpdateAppointment(appointment: AppointmentDto)
}


