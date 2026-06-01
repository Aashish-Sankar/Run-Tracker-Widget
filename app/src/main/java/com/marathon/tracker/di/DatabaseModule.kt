package com.marathon.tracker.di

import android.content.Context
import androidx.room.Room
import com.marathon.tracker.data.local.AppDatabase
import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.dao.TrainingPlanDao
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "marathon_db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideWorkoutLogDao(db: AppDatabase): WorkoutLogDao = db.workoutLogDao()

    @Provides
    fun provideStravaActivityDao(db: AppDatabase): StravaActivityDao = db.stravaActivityDao()

    @Provides
    fun provideTrainingPlanDao(db: AppDatabase): TrainingPlanDao = db.trainingPlanDao()
}
