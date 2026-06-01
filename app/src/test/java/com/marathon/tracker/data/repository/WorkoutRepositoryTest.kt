package com.marathon.tracker.data.repository

import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import com.marathon.tracker.data.local.entity.StravaActivityEntity
import com.marathon.tracker.data.local.entity.WorkoutLogEntity
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TrainingPhase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WorkoutRepositoryTest {

    private val workoutLogDao: WorkoutLogDao = mockk()
    private val stravaActivityDao: StravaActivityDao = mockk()
    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setUp() {
        // Provide stubs for flows used in getTodayWorkout / getWeekSummary
        every { workoutLogDao.getWeeklyActualKm(any()) } returns flowOf(0.0)
        every { workoutLogDao.getCompletedDaysInWeek(any()) } returns flowOf(0)
        every { stravaActivityDao.getLastActivity() } returns flowOf(null)
        coEvery { workoutLogDao.getLogForDate(any()) } returns null
        coEvery { stravaActivityDao.getRunForDate(any()) } returns null

        repository = WorkoutRepositoryImpl(workoutLogDao, stravaActivityDao)
    }

    private fun stravaActivity(distanceKm: Double = 10.0, type: String = "Run") = StravaActivity(
        id = 1L,
        name = "Morning Run",
        type = type,
        startDate = LocalDate.of(2026, 6, 1),
        distanceKm = distanceKm,
        movingTimeSeconds = 3600,
        averagePaceSecPerKm = 360.0,
        averageHeartrate = 145.0,
        maxHeartrate = 165.0,
        totalElevationGain = 50.0,
        kudosCount = 0,
        mapPolyline = null,
    )

    private fun dayWorkout(runType: RunType = RunType.EASY, distanceKm: Double = 10.0) = DayWorkout(
        date = LocalDate.of(2026, 6, 1),
        weekNumber = 1,
        dayOfWeek = DayOfWeek.MONDAY,
        phase = TrainingPhase.BASE_BUILDING,
        runType = runType,
        distanceKm = distanceKm,
        paceRange = null,
        gymSession = null,
        coachNote = null,
    )

    @Test
    fun `matchStravaActivityToPlanDay returns true for same distance run`() {
        assertTrue(repository.matchStravaActivityToPlanDay(stravaActivity(10.0), dayWorkout(distanceKm = 10.0)))
    }

    @Test
    fun `matchStravaActivityToPlanDay returns true within 3km tolerance`() {
        assertTrue(repository.matchStravaActivityToPlanDay(stravaActivity(12.9), dayWorkout(distanceKm = 10.0)))
        assertTrue(repository.matchStravaActivityToPlanDay(stravaActivity(7.1), dayWorkout(distanceKm = 10.0)))
    }

    @Test
    fun `matchStravaActivityToPlanDay returns false beyond 3km tolerance`() {
        assertFalse(repository.matchStravaActivityToPlanDay(stravaActivity(14.0), dayWorkout(distanceKm = 10.0)))
    }

    @Test
    fun `matchStravaActivityToPlanDay returns false for REST plan`() {
        assertFalse(repository.matchStravaActivityToPlanDay(stravaActivity(), dayWorkout(runType = RunType.REST, distanceKm = 0.0)))
    }

    @Test
    fun `matchStravaActivityToPlanDay returns false for GYM_ONLY plan`() {
        assertFalse(repository.matchStravaActivityToPlanDay(stravaActivity(), dayWorkout(runType = RunType.GYM_ONLY, distanceKm = 0.0)))
    }

    @Test
    fun `matchStravaActivityToPlanDay returns false for non-run activity`() {
        assertFalse(repository.matchStravaActivityToPlanDay(stravaActivity(type = "Ride"), dayWorkout()))
    }

    @Test
    fun `getCurrentWeekNumber returns positive int for plan start date`() {
        // The plan started on 2026-06-01, current date is 2026-06-01 per system-reminder
        val weekNum = repository.getCurrentWeekNumber()
        assertTrue("Week number should be >= 1", weekNum >= 1)
    }

    @Test
    fun `getAllWeeks returns 35 weeks`() {
        val weeks = repository.getAllWeeks()
        assertTrue("Should have 35 weeks", weeks.size == 35)
    }

    @Test
    fun `getWorkoutForDate returns null for date before plan`() {
        val result = repository.getWorkoutForDate(LocalDate.of(2020, 1, 1))
        assertTrue(result == null)
    }

    @Test
    fun `getWeekPlan returns null for out-of-range week`() {
        val result = repository.getWeekPlan(99)
        assertTrue(result == null)
    }
}
