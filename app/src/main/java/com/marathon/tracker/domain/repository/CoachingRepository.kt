package com.marathon.tracker.domain.repository

import com.marathon.tracker.domain.model.CoachingReport
import kotlinx.coroutines.flow.Flow

interface CoachingRepository {
    fun getLatestReport(): Flow<CoachingReport?>
    suspend fun generateReport(weekNumber: Int): Result<CoachingReport>
    fun canGenerateReport(): Flow<Boolean>
}
