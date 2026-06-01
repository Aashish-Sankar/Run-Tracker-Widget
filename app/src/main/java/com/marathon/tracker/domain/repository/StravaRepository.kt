package com.marathon.tracker.domain.repository

import com.marathon.tracker.domain.model.AthleteInfo
import com.marathon.tracker.domain.model.StravaActivity
import kotlinx.coroutines.flow.Flow

interface StravaRepository {
    suspend fun syncActivities(): Result<Int>
    fun getRecentActivities(limit: Int = 30): Flow<List<StravaActivity>>
    fun getLastActivity(): Flow<StravaActivity?>
    fun isConnected(): Boolean
    suspend fun getAthleteInfo(): Result<AthleteInfo>
}
