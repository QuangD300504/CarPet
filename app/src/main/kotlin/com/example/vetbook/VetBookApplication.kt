package com.example.vetbook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VetBookApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build())
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở VetBook",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nhắc lịch tiêm chủng và lịch khám thú cưng"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "vetbook_reminders"
    }
}
