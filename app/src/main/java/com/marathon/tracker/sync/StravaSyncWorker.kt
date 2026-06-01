package com.marathon.tracker.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.marathon.tracker.domain.usecase.SyncStravaActivitiesUseCase
import com.marathon.tracker.util.MarathonNotificationManager
import com.marathon.tracker.widget.MarathonWidgetReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class StravaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncStravaActivitiesUseCase: SyncStravaActivitiesUseCase,
    private val notificationManager: MarathonNotificationManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = syncStravaActivitiesUseCase()
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                notificationManager.showSyncCompleteNotification(count)
                MarathonWidgetReceiver.updateAll(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "strava_sync_one_time"

        fun buildRequest() = OneTimeWorkRequestBuilder<StravaSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
    }
}
