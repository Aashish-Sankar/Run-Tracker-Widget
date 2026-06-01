package com.marathon.tracker.di

import com.marathon.tracker.data.repository.CoachingRepositoryImpl
import com.marathon.tracker.data.repository.StravaRepositoryImpl
import com.marathon.tracker.data.repository.UserPreferencesRepositoryImpl
import com.marathon.tracker.data.repository.WorkoutRepositoryImpl
import com.marathon.tracker.domain.repository.CoachingRepository
import com.marathon.tracker.domain.repository.StravaRepository
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindStravaRepository(impl: StravaRepositoryImpl): StravaRepository

    @Binds
    @Singleton
    abstract fun bindCoachingRepository(impl: CoachingRepositoryImpl): CoachingRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
