package com.marathon.tracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.marathon.tracker.domain.model.AthleteInfo
import com.marathon.tracker.domain.model.ThemeMode
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    private object Keys {
        val REFRESH_INTERVAL = intPreferencesKey("widget_refresh_interval_min")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ATHLETE_ID = longPreferencesKey("athlete_id")
        val ATHLETE_FIRST_NAME = stringPreferencesKey("athlete_first_name")
        val ATHLETE_LAST_NAME = stringPreferencesKey("athlete_last_name")
        val ATHLETE_PROFILE_URL = stringPreferencesKey("athlete_profile_url")
        val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LAST_REPORT_EPOCH_DAY = longPreferencesKey("last_report_epoch_day")
    }

    override fun getRefreshIntervalMinutes(): Flow<Int> =
        dataStore.data.map { it[Keys.REFRESH_INTERVAL] ?: 30 }

    override suspend fun setRefreshIntervalMinutes(minutes: Int) {
        dataStore.edit { it[Keys.REFRESH_INTERVAL] = minutes }
    }

    override fun getThemeMode(): Flow<ThemeMode> =
        dataStore.data.map {
            when (it[Keys.THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override fun getAthleteInfo(): Flow<AthleteInfo?> =
        dataStore.data.map { prefs ->
            val id = prefs[Keys.ATHLETE_ID] ?: return@map null
            AthleteInfo(
                id = id,
                firstName = prefs[Keys.ATHLETE_FIRST_NAME] ?: "",
                lastName = prefs[Keys.ATHLETE_LAST_NAME] ?: "",
                profileUrl = prefs[Keys.ATHLETE_PROFILE_URL] ?: "",
            )
        }

    override suspend fun saveAthleteInfo(info: AthleteInfo) {
        dataStore.edit {
            it[Keys.ATHLETE_ID] = info.id
            it[Keys.ATHLETE_FIRST_NAME] = info.firstName
            it[Keys.ATHLETE_LAST_NAME] = info.lastName
            it[Keys.ATHLETE_PROFILE_URL] = info.profileUrl
        }
    }

    override fun getClaudeApiKey(): Flow<String?> =
        dataStore.data.map { it[stringPreferencesKey("claude_api_key")] }

    override suspend fun setClaudeApiKey(key: String) {
        dataStore.edit { it[stringPreferencesKey("claude_api_key")] = key }
    }

    override fun getLastSyncTimeMillis(): Flow<Long?> =
        dataStore.data.map { it[Keys.LAST_SYNC_MILLIS] }

    override suspend fun setLastSyncTimeMillis(timeMillis: Long) {
        dataStore.edit { it[Keys.LAST_SYNC_MILLIS] = timeMillis }
    }

    override fun areNotificationsEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override fun getLastReportDateEpochDay(): Flow<Long?> =
        dataStore.data.map { it[Keys.LAST_REPORT_EPOCH_DAY] }

    override suspend fun setLastReportDateEpochDay(epochDay: Long) {
        dataStore.edit { it[Keys.LAST_REPORT_EPOCH_DAY] = epochDay }
    }
}
