package com.marathon.tracker.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.WorkManager
import com.marathon.tracker.sync.WidgetUpdateWorker

class MarathonWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = MarathonWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedule(WorkManager.getInstance(context))
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork(WidgetUpdateWorker.WORK_NAME)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateWorker.schedule(WorkManager.getInstance(context))
    }

    companion object {
        fun updateAll(context: Context) {
            val intent = Intent(context, MarathonWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
