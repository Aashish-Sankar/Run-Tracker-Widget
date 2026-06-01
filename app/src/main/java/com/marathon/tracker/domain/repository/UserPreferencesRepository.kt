package com.marathon.tracker.domain.repository

import com.marathon.tracker.domain.model.AthleteInfo
import com.marathon.tracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getRefreshIntervalMinutes(): Flow<Int>
    suspend fun setRefreshIntervalMinutes(minutes: Int)
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    fun getAthleteInfo(): Flow<AthleteInfo?>
    suspend fun saveAthleteInfo(info: AthleteInfo)
    fun getClaudeApiKey(): Flow<String?>
    suspend fun setClaudeApiKey(key: String)
    fun getLastSyncTimeMillis(): Flow<Long?>
    suspend fun setLastSyncTimeMillis(timeMillis: Long)
    fun areNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun getLastReportDateEpochDay(): Flow<Long?>
    suspend fun setLastReportDateEpochDay(epochDay: Long)
}
