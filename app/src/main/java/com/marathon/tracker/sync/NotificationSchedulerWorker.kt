package com.marathon.tracker.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.repository.WorkoutRepository
import com.marathon.tracker.util.MarathonNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationSchedulerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workoutRepository: WorkoutRepository,
    private val notificationManager: MarathonNotificationManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val todayPlan = workoutRepository.getWorkoutForDate(today)

        when (inputData.getString(KEY_TYPE)) {
            TYPE_MORNING -> {
                if (todayPlan != null && todayPlan.runType != RunType.REST) {
                    val nextRace = WorkoutData.RACES.filter { it.date >= today }.minByOrNull { it.date }
                    val daysToRace = nextRace?.let {
                        java.time.temporal.ChronoUnit.DAYS.between(today, it.date).toInt()
                    } ?: -1

                    if (nextRace != null && daysToRace in 0..7) {
                        notificationManager.showPreRaceNotification(nextRace, daysToRace)
                    } else {
                        notificationManager.showDailyWorkoutNotification(todayPlan)
                    }
                }
            }
            TYPE_EVENING -> {
                if (todayPlan != null && todayPlan.runType != RunType.REST && todayPlan.runType != RunType.GYM_ONLY) {
                    notificationManager.showMissedRunNotification(todayPlan)
                }
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME_MORNING = "morning_notification"
        private const val WORK_NAME_EVENING = "evening_notification"
        const val KEY_TYPE = "notification_type"
        const val TYPE_MORNING = "morning"
        const val TYPE_EVENING = "evening"

        fun scheduleMorning(workManager: WorkManager) {
            val delayMinutes = minutesUntil(LocalTime.of(6, 50))
            val data = androidx.work.Data.Builder().putString(KEY_TYPE, TYPE_MORNING).build()
            val request = PeriodicWorkRequestBuilder<NotificationSchedulerWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME_MORNING, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun scheduleEvening(workManager: WorkManager) {
            val delayMinutes = minutesUntil(LocalTime.of(21, 0))
            val data = androidx.work.Data.Builder().putString(KEY_TYPE, TYPE_EVENING).build()
            val request = PeriodicWorkRequestBuilder<NotificationSchedulerWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME_EVENING, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        private fun minutesUntil(target: LocalTime): Long {
            val now = LocalTime.now()
            return if (now.isBefore(target)) {
                Duration.between(now, target).toMinutes()
            } else {
                Duration.between(now, target).toMinutes() + 24 * 60
            }
        }
    }
}
