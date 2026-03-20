package com.example.vetbook.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vetbook.notification.ReminderNotificationHelper

class AppointmentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val vetName = inputData.getString("vetName") ?: return Result.failure()
        val petName = inputData.getString("petName") ?: return Result.failure()
        val time = inputData.getString("time") ?: return Result.failure()

        ReminderNotificationHelper.showNotification(
            context = applicationContext,
            title = "Nhắc lịch khám",
            body = "Lịch khám với $vetName cho $petName vào $time"
        )

        return Result.success()
    }
}
