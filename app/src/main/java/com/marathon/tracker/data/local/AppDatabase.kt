package com.marathon.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import com.marathon.tracker.data.local.entity.StravaActivityEntity
import com.marathon.tracker.data.local.entity.WorkoutLogEntity

@Database(
    entities = [WorkoutLogEntity::class, StravaActivityEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun stravaActivityDao(): StravaActivityDao
}
