package com.marathon.tracker.domain.usecase

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TrainingPhase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class MatchActivityToPlanUseCaseTest {

    private lateinit var useCase: MatchActivityToPlanUseCase

    @Before
    fun setUp() {
        useCase = MatchActivityToPlanUseCase()
    }

    private fun activity(type: String = "Run", distanceKm: Double = 10.0) = StravaActivity(
        id = 1L,
        name = "Test Run",
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

    private fun plan(runType: RunType = RunType.EASY, distanceKm: Double = 10.0) = DayWorkout(
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
    fun `matches when activity is Run and distance within 3km`() {
        assertTrue(useCase(activity(distanceKm = 10.0), plan(distanceKm = 10.0)))
    }

    @Test
    fun `matches when activity distance is exactly 3km under plan`() {
        assertTrue(useCase(activity(distanceKm = 7.0), plan(distanceKm = 10.0)))
    }

    @Test
    fun `matches when activity distance is exactly 3km over plan`() {
        assertTrue(useCase(activity(distanceKm = 13.0), plan(distanceKm = 10.0)))
    }

    @Test
    fun `does not match when distance difference exceeds 3km`() {
        assertFalse(useCase(activity(distanceKm = 6.9), plan(distanceKm = 10.0)))
    }

    @Test
    fun `does not match when plan is REST`() {
        assertFalse(useCase(activity(), plan(runType = RunType.REST)))
    }

    @Test
    fun `does not match when plan is GYM_ONLY`() {
        assertFalse(useCase(activity(), plan(runType = RunType.GYM_ONLY)))
    }

    @Test
    fun `does not match when activity type is not Run`() {
        assertFalse(useCase(activity(type = "Ride"), plan()))
    }

    @Test
    fun `does not match when plan distance is zero`() {
        assertFalse(useCase(activity(distanceKm = 0.0), plan(distanceKm = 0.0)))
    }
}
