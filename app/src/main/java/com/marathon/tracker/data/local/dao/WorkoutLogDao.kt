package com.marathon.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.marathon.tracker.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {

    @Query("SELECT * FROM workout_logs WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getLogForDate(epochDay: Long): WorkoutLogEntity?

    @Query("SELECT * FROM workout_logs WHERE weekNumber = :week")
    fun getLogsForWeek(week: Int): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs WHERE dateEpochDay BETWEEN :startDay AND :endDay ORDER BY dateEpochDay")
    fun getLogsForDateRange(startDay: Long, endDay: Long): Flow<List<WorkoutLogEntity>>

    @Upsert
    suspend fun upsertLog(log: WorkoutLogEntity)

    @Query("UPDATE workout_logs SET isCompleted = 1, completedAtMillis = :millis WHERE id = :id")
    suspend fun markCompleted(id: String, millis: Long)

    @Query("SELECT COALESCE(SUM(actualDistanceKm), 0.0) FROM workout_logs WHERE weekNumber = :week AND isCompleted = 1")
    fun getWeeklyActualKm(week: Int): Flow<Double>

    @Query("SELECT COUNT(*) FROM workout_logs WHERE weekNumber = :week AND isCompleted = 1")
    fun getCompletedDaysInWeek(week: Int): Flow<Int>
}
