package com.marathon.tracker.domain.usecase

import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WidgetState
import com.marathon.tracker.domain.repository.StravaRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetWidgetStateUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val stravaRepository: StravaRepository,
) {
    suspend operator fun invoke(): WidgetState {
        val today = LocalDate.now()
        val todayWorkout = workoutRepository.getWorkoutForDate(today)
        val weekNumber = workoutRepository.getCurrentWeekNumber()
        val weekSummary = workoutRepository.getWeekSummary(weekNumber).first()
        val lastActivity = stravaRepository.getLastActivity().first()
        val isConnected = stravaRepository.isConnected()

        val nextRace = WorkoutData.RACES.firstOrNull { it.date >= today }
            ?: WorkoutData.RACES.last()
        val daysToNextRace = ChronoUnit.DAYS.between(today, nextRace.date)
            .toInt().coerceAtLeast(0)

        val completedRaces = WorkoutData.RACES
            .filter { it.date < today }
            .map { it.name }

        return WidgetState(
            todayWorkout = todayWorkout,
            lastStravaActivity = lastActivity,
            daysToNextRace = daysToNextRace,
            nextRaceName = nextRace.name,
            weekNumber = weekNumber,
            weeklyKmTarget = weekSummary.plannedKm,
            weeklyKmDone = weekSummary.actualKm,
            completedRaces = completedRaces,
            isStravaConnected = isConnected,
            lastUpdated = System.currentTimeMillis(),
            currentPhase = todayWorkout?.phase ?: TrainingPhase.BASE_BUILDING,
        )
    }
}
