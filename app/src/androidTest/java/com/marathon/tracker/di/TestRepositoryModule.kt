package com.marathon.tracker.di

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekSummary
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.repository.CoachingRepository
import com.marathon.tracker.domain.repository.StravaRepository
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// Fake WorkoutRepository for UI tests
@Singleton
class FakeWorkoutRepository @Inject constructor() : WorkoutRepository {

    val todayWorkoutFlow = MutableStateFlow(
        TodayWorkout(
            plan = DayWorkout(
                date = LocalDate.now(),
                weekNumber = 1,
                dayOfWeek = DayOfWeek.MONDAY,
                phase = TrainingPhase.BASE_BUILDING,
                runType = RunType.EASY,
                distanceKm = 10.0,
                paceRange = null,
                gymSession = null,
                coachNote = "Keep it easy today.",
            ),
            logEntry = null,
            matchedStravaActivity = null,
            isCompleted = false,
        )
    )

    override fun getTodayWorkout(): Flow<TodayWorkout> = todayWorkoutFlow

    override fun getWeekSummary(weekNumber: Int): Flow<WeekSummary> = flowOf(
        WeekSummary(
            weekNumber = 1,
            plannedKm = 42.0,
            actualKm = 18.0,
            completedDays = 2,
            totalDays = 7,
            phase = TrainingPhase.BASE_BUILDING,
            startDate = LocalDate.now(),
        )
    )

    override suspend fun markWorkoutCompleted(date: LocalDate, actualKm: Double, actualPaceSecPerKm: Double) {}

    override fun getWorkoutForDate(date: LocalDate): DayWorkout? = null

    override fun getAllWeeks(): List<WeekPlan> = emptyList()

    override fun getWeekPlan(weekNumber: Int): WeekPlan? = null

    override fun getCurrentWeekNumber(): Int = 1
}

// Fake StravaRepository for UI tests
@Singleton
class FakeStravaRepository @Inject constructor() : StravaRepository {
    override suspend fun syncActivities(): Result<Int> = Result.success(0)
    override fun getRecentActivities(limit: Int): Flow<List<StravaActivity>> = flowOf(emptyList())
    override fun getLastActivity(): Flow<StravaActivity?> = flowOf(null)
    override fun isConnected(): Boolean = false
    override suspend fun getAthleteInfo(): Result<com.marathon.tracker.domain.model.AthleteInfo> =
        Result.failure(UnsupportedOperationException("Test stub"))
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
abstract class TestRepositoryModule {
    @Binds @Singleton
    abstract fun bindWorkoutRepository(fake: FakeWorkoutRepository): WorkoutRepository

    @Binds @Singleton
    abstract fun bindStravaRepository(fake: FakeStravaRepository): StravaRepository
}
