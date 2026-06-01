package com.marathon.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.marathon.tracker.data.local.entity.StravaActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StravaActivityDao {

    @Query("SELECT * FROM strava_activities ORDER BY startDateMillis DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 30): Flow<List<StravaActivityEntity>>

    @Query("SELECT * FROM strava_activities WHERE startDateEpochDay = :epochDay AND type = 'Run' LIMIT 1")
    suspend fun getRunForDate(epochDay: Long): StravaActivityEntity?

    @Query("SELECT * FROM strava_activities WHERE startDateEpochDay BETWEEN :startDay AND :endDay AND type = 'Run' ORDER BY startDateMillis DESC")
    suspend fun getRunsForDateRange(startDay: Long, endDay: Long): List<StravaActivityEntity>

    @Query("SELECT * FROM strava_activities ORDER BY startDateMillis DESC LIMIT 1")
    fun getLastActivity(): Flow<StravaActivityEntity?>

    @Query("SELECT MAX(startDateMillis) FROM strava_activities")
    suspend fun getLatestActivityTimestampMillis(): Long?

    @Upsert
    suspend fun upsertActivities(activities: List<StravaActivityEntity>)

    @Query("DELETE FROM strava_activities WHERE startDateMillis < :cutoffMillis")
    suspend fun deleteBefore(cutoffMillis: Long)
}
