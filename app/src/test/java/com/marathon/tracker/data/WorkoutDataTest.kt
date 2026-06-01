package com.marathon.tracker.data

import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkoutDataTest {

    @Test
    fun `ALL_WEEKS contains exactly 35 weeks`() {
        assertEquals(35, WorkoutData.ALL_WEEKS.size)
    }

    @Test
    fun `each week has exactly 7 days`() {
        WorkoutData.ALL_WEEKS.forEach { week ->
            assertEquals("Week ${week.weekNumber} should have 7 days", 7, week.days.size)
        }
    }

    @Test
    fun `weeks are numbered 1 to 35`() {
        val weekNumbers = WorkoutData.ALL_WEEKS.map { it.weekNumber }
        assertEquals((1..35).toList(), weekNumbers)
    }

    @Test
    fun `plan starts on 2026-06-01`() {
        val firstDay = WorkoutData.ALL_WEEKS.first().days.first()
        assertEquals(LocalDate.of(2026, 6, 1), firstDay.date)
    }

    @Test
    fun `half marathon race day is Oct 18 2026`() {
        val hmDate = LocalDate.of(2026, 10, 18)
        val workout = WorkoutData.getWorkoutForDate(hmDate)
        assertNotNull(workout)
        assertTrue("HM day should be a race day", workout!!.isRaceDay)
        assertEquals(RunType.RACE, workout.runType)
    }

    @Test
    fun `25K race day is Dec 20 2026`() {
        val raceDate = LocalDate.of(2026, 12, 20)
        val workout = WorkoutData.getWorkoutForDate(raceDate)
        assertNotNull(workout)
        assertTrue(workout!!.isRaceDay)
    }

    @Test
    fun `full marathon race day is Jan 17 2027`() {
        val raceDate = LocalDate.of(2027, 1, 17)
        val workout = WorkoutData.getWorkoutForDate(raceDate)
        assertNotNull(workout)
        assertTrue(workout!!.isRaceDay)
    }

    @Test
    fun `getWorkoutForDate returns null for date before plan`() {
        val beforePlan = LocalDate.of(2026, 5, 31)
        assertNull(WorkoutData.getWorkoutForDate(beforePlan))
    }

    @Test
    fun `first 6 weeks are BASE_BUILDING phase`() {
        (1..6).forEach { weekNum ->
            val week = WorkoutData.ALL_WEEKS[weekNum - 1]
            assertEquals("Week $weekNum should be BASE_BUILDING", TrainingPhase.BASE_BUILDING, week.phase)
        }
    }

    @Test
    fun `weeks 7 to 12 are AEROBIC_DEVELOPMENT phase`() {
        (7..12).forEach { weekNum ->
            val week = WorkoutData.ALL_WEEKS[weekNum - 1]
            assertEquals("Week $weekNum should be AEROBIC_DEVELOPMENT", TrainingPhase.AEROBIC_DEVELOPMENT, week.phase)
        }
    }

    @Test
    fun `RACES list contains 3 races`() {
        assertEquals(3, WorkoutData.RACES.size)
    }

    @Test
    fun `getWeekNumberForDate returns correct week for plan start`() {
        val planStart = LocalDate.of(2026, 6, 1)
        assertEquals(1, WorkoutData.getWeekNumberForDate(planStart))
    }

    @Test
    fun `totalKm per week is positive`() {
        WorkoutData.ALL_WEEKS.forEach { week ->
            assertTrue("Week ${week.weekNumber} should have positive total km", week.totalPlannedKm > 0)
        }
    }

    @Test
    fun `no day has negative distance`() {
        WorkoutData.ALL_WEEKS.flatMap { it.days }.forEach { day ->
            assertTrue("Day ${day.date} should not have negative distance", day.distanceKm >= 0)
        }
    }
}
