package com.marathon.tracker.domain.usecase

import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) {
    operator fun invoke(): Flow<TodayWorkout> = workoutRepository.getTodayWorkout()
}
