package com.marathon.tracker.util

import com.marathon.tracker.data.workout.WorkoutData
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DateUtils {

    fun getWeekNumberForDate(date: LocalDate): Int =
        WorkoutData.getWeekNumberForDate(date)

    fun getDaysUntil(targetDate: LocalDate): Int =
        ChronoUnit.DAYS.between(LocalDate.now(), targetDate).toInt().coerceAtLeast(0)

    fun formatRelativeTime(epochMillis: Long): String {
        val minutesAgo = (System.currentTimeMillis() - epochMillis) / 60_000
        return when {
            minutesAgo < 1 -> "Just now"
            minutesAgo < 60 -> "${minutesAgo}m ago"
            minutesAgo < 1440 -> "${minutesAgo / 60}h ago"
            else -> "${minutesAgo / 1440}d ago"
        }
    }

    fun Long.toLocalDate(): LocalDate =
        java.time.Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    fun LocalDate.toEpochMilli(): Long =
        atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun formatDate(date: LocalDate): String =
        date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d"))

    fun formatDateFull(date: LocalDate): String =
        date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))
}
