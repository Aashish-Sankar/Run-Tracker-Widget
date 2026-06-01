package com.marathon.tracker.domain.usecase

import com.marathon.tracker.domain.model.CoachingReport
import com.marathon.tracker.domain.repository.CoachingRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import javax.inject.Inject

class GenerateCoachingReportUseCase @Inject constructor(
    private val coachingRepository: CoachingRepository,
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(): Result<CoachingReport> {
        val weekNumber = workoutRepository.getCurrentWeekNumber()
        return coachingRepository.generateReport(weekNumber)
    }
}
