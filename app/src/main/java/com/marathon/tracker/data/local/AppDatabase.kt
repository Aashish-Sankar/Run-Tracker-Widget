package com.marathon.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.dao.TrainingPlanDao
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import com.marathon.tracker.data.local.entity.StravaActivityEntity
import com.marathon.tracker.data.local.entity.TrainingPlanEntity
import com.marathon.tracker.data.local.entity.WorkoutLogEntity

@Database(
    entities = [WorkoutLogEntity::class, StravaActivityEntity::class, TrainingPlanEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun stravaActivityDao(): StravaActivityDao
    abstract fun trainingPlanDao(): TrainingPlanDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS training_plans (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        startDateEpochDay INTEGER NOT NULL,
                        targetMarathonSeconds INTEGER,
                        racesJson TEXT NOT NULL,
                        weeksJson TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
