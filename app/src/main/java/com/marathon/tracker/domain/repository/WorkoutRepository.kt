package com.marathon.tracker.domain.repository

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.model.WeekSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WorkoutRepository {
    fun getTodayWorkout(): Flow<TodayWorkout>
    fun getWeekSummary(weekNumber: Int): Flow<WeekSummary>
    suspend fun markWorkoutCompleted(date: LocalDate, actualKm: Double, actualPaceSecPerKm: Double)
    fun getWorkoutForDate(date: LocalDate): DayWorkout?
    fun getAllWeeks(): List<WeekPlan>
    fun getWeekPlan(weekNumber: Int): WeekPlan?
    fun getCurrentWeekNumber(): Int
}
