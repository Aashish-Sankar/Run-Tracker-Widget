package com.marathon.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val weekNumber: Int,
    val plannedDistanceKm: Double,
    val plannedRunType: String,
    val actualDistanceKm: Double?,
    val actualPaceSecPerKm: Double?,
    val stravaActivityId: Long?,
    val isCompleted: Boolean,
    val notes: String?,
    val completedAtMillis: Long?,
)
