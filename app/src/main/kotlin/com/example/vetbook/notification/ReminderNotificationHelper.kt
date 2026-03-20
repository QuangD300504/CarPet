package com.example.vetbook.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.vetbook.MainActivity
import com.example.vetbook.R
import com.example.vetbook.VetBookApplication
import com.example.vetbook.workers.AppointmentReminderWorker
import com.example.vetbook.workers.VaccinationReminderWorker
import java.util.concurrent.TimeUnit

object ReminderNotificationHelper {

    private const val CHANNEL_ID = "vetbook_reminders"

    fun scheduleVaccinationReminder(
        context: Context,
        workName: String,
        petName: String,
        vaccineName: String,
        dueDate: String,
        reminderTimeMillis: Long
    ) {
        val delay = reminderTimeMillis - System.currentTimeMillis()
        if (delay <= 0) return

        if (!hasNotificationPermission(context)) return

        val input = workDataOf(
            "petName" to petName,
            "vaccineName" to vaccineName,
            "dueDate" to dueDate
        )

        val request = OneTimeWorkRequestBuilder<VaccinationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelVaccinationReminder(context: Context, workName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    fun scheduleAppointmentReminder(
        context: Context,
        workName: String,
        vetName: String,
        petName: String,
        appointmentTime: String,
        reminderTimeMillis: Long
    ) {
        val delay = reminderTimeMillis - System.currentTimeMillis()
        if (delay <= 0) return

        if (!hasNotificationPermission(context)) return

        val input = workDataOf(
            "vetName" to vetName,
            "petName" to petName,
            "time" to appointmentTime
        )

        val request = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelAppointmentReminder(context: Context, workName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
