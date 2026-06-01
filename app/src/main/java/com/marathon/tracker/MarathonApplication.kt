package com.marathon.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.domain.repository.PlanRepository
import com.marathon.tracker.sync.NotificationSchedulerWorker
import com.marathon.tracker.util.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MarathonApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var planRepository: PlanRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createAllChannels(this)
        val wm = WorkManager.getInstance(this)
        NotificationSchedulerWorker.scheduleMorning(wm)
        NotificationSchedulerWorker.scheduleEvening(wm)
        appScope.launch {
            planRepository.seedDefaultIfNeeded(WorkoutData.ALL_WEEKS, WorkoutData.RACES)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
