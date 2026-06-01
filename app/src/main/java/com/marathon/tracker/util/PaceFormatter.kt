package com.marathon.tracker.util

import com.marathon.tracker.domain.model.PaceRange
import kotlin.math.abs
import kotlin.math.roundToInt

object PaceFormatter {

    fun formatPace(secPerKm: Double): String {
        if (secPerKm <= 0) return "--:--/km"
        val totalSecs = secPerKm.roundToInt()
        val minutes = totalSecs / 60
        val seconds = totalSecs % 60
        return "%d:%02d/km".format(minutes, seconds)
    }

    fun formatPaceRange(range: PaceRange): String {
        val fast = formatPace(range.minSecondsPerKm.toDouble())
        val slow = formatPace(range.maxSecondsPerKm.toDouble())
        return if (range.minSecondsPerKm == range.maxSecondsPerKm) fast
        else "$fast – $slow"
    }

    fun paceDeltaText(actualSecPerKm: Double, targetSecPerKm: Double): String {
        val diff = actualSecPerKm - targetSecPerKm
        val absDiff = abs(diff).roundToInt()
        return if (diff < 0) "-${absDiff}s/km" else "+${absDiff}s/km"
    }

    fun speedMpsToSecPerKm(speedMps: Double): Double {
        if (speedMps <= 0) return 0.0
        return 1000.0 / speedMps
    }

    fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs)
        else "%d:%02d".format(minutes, secs)
    }
}
