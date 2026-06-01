package com.marathon.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.marathon.tracker.sync.NotificationSchedulerWorker
import com.marathon.tracker.util.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MarathonApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createAllChannels(this)
        val wm = WorkManager.getInstance(this)
        NotificationSchedulerWorker.scheduleMorning(wm)
        NotificationSchedulerWorker.scheduleEvening(wm)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
