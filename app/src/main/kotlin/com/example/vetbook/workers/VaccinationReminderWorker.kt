package com.example.vetbook.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vetbook.notification.ReminderNotificationHelper

class VaccinationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val petName = inputData.getString("petName") ?: return Result.failure()
        val vaccineName = inputData.getString("vaccineName") ?: return Result.failure()
        val dueDate = inputData.getString("dueDate") ?: "ngày mai"

        ReminderNotificationHelper.showNotification(
            context = applicationContext,
            title = "Nhắc tiêm phòng",
            body = "$petName cần tiêm $vaccineName vào $dueDate!"
        )

        return Result.success()
    }
}
