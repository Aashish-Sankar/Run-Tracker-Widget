package com.marathon.tracker.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.marathon.tracker.MainActivity
import com.marathon.tracker.R
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.util.NotificationChannelManager.NotificationIds
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarathonNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    private fun canPost(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showDailyWorkoutNotification(workout: DayWorkout) {
        if (!canPost()) return
        val title = "Today: ${workout.runType.displayName}"
        val text = when {
            workout.distanceKm > 0 -> "${workout.distanceKm}km"
            workout.gymSession != null -> workout.gymSession.focus
            else -> "Rest day"
        }
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_DAILY_WORKOUT)
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(buildTodayPendingIntent())
            .build()
        notificationManager.notify(NotificationIds.DAILY_WORKOUT, notification)
    }

    fun showSyncCompleteNotification(count: Int) {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_STRAVA_SYNC)
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentTitle("Strava synced")
            .setContentText("$count activities updated")
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NotificationIds.STRAVA_SYNC, notification)
    }

    fun showPreRaceNotification(race: Race, daysUntil: Int) {
        if (!canPost()) return
        val title = "${race.name} in $daysUntil day${if (daysUntil == 1) "" else "s"}"
        val text = "Check your race prep checklist"
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_PRE_RACE)
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(NotificationIds.PRE_RACE_BASE + daysUntil, notification)
    }

    fun showCoachingReadyNotification() {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_COACHING_READY)
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentTitle("Weekly coaching report ready")
            .setContentText("Tap to see your AI coaching analysis")
            .setAutoCancel(true)
            .setContentIntent(buildCoachingPendingIntent())
            .build()
        notificationManager.notify(NotificationIds.COACHING_READY, notification)
    }

    fun showMissedRunNotification(workout: DayWorkout) {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_MISSED_RUN)
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentTitle("Run not logged yet")
            .setContentText("${workout.runType.displayName} ${workout.distanceKm}km — tap to mark as done or rest day")
            .setAutoCancel(true)
            .setContentIntent(buildTodayPendingIntent())
            .build()
        notificationManager.notify(NotificationIds.MISSED_RUN, notification)
    }

    private fun buildTodayPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("marathon://today")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun buildCoachingPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("marathon://coaching")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
