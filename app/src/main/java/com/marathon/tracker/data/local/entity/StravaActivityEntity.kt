package com.marathon.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strava_activities")
data class StravaActivityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val startDateEpochDay: Long,
    val startDateMillis: Long,
    val distanceMeters: Double,
    val movingTimeSeconds: Int,
    val averageSpeedMps: Double,
    val averageHeartrate: Double?,
    val maxHeartrate: Double?,
    val totalElevationGain: Double,
    val kudosCount: Int,
    val mapPolyline: String?,
    val syncedAtMillis: Long,
)
