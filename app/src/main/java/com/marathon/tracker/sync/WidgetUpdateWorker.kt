package com.marathon.tracker.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.marathon.tracker.domain.usecase.GetWidgetStateUseCase
import com.marathon.tracker.widget.MarathonWidgetReceiver
import com.marathon.tracker.widget.WidgetStateManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getWidgetStateUseCase: GetWidgetStateUseCase,
    private val widgetStateManager: WidgetStateManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val state = getWidgetStateUseCase()
            widgetStateManager.updateState(state)
            MarathonWidgetReceiver.updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "widget_update_periodic"

        fun schedule(workManager: WorkManager, intervalMinutes: Long = 30) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
                .setConstraints(Constraints.Builder().build())
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
