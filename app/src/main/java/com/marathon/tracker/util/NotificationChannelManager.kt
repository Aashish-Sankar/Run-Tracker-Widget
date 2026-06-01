package com.marathon.tracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat

object NotificationChannelManager {

    const val CHANNEL_DAILY_WORKOUT = "daily_workout"
    const val CHANNEL_PRE_RACE = "pre_race"
    const val CHANNEL_STRAVA_SYNC = "strava_sync"
    const val CHANNEL_COACHING_READY = "coaching_ready"
    const val CHANNEL_MISSED_RUN = "missed_run"

    object NotificationIds {
        const val DAILY_WORKOUT = 1001
        const val STRAVA_SYNC = 1002
        const val COACHING_READY = 1003
        const val MISSED_RUN = 1004
        const val PRE_RACE_BASE = 2000
    }

    fun createAllChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            NotificationChannel(
                CHANNEL_DAILY_WORKOUT,
                "Daily Workout Reminder",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "7 AM reminder for today's planned workout" },
            NotificationChannel(
                CHANNEL_PRE_RACE,
                "Race Day Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Alerts 7, 3, and 1 day before each race" },
            NotificationChannel(
                CHANNEL_STRAVA_SYNC,
                "Strava Sync",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Background sync status" },
            NotificationChannel(
                CHANNEL_COACHING_READY,
                "AI Coaching Ready",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Weekly coaching report available" },
            NotificationChannel(
                CHANNEL_MISSED_RUN,
                "Missed Run Alert",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "9 PM alert if today's run wasn't logged" },
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
