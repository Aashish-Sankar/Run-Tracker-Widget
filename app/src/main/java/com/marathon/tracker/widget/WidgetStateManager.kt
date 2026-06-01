package com.marathon.tracker.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.marathon.tracker.domain.model.WidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val WIDGET_STATE_KEY = stringPreferencesKey("widget_state_json")
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }

    fun getWidgetStateFlow(): Flow<WidgetState?> =
        dataStore.data.map { prefs ->
            prefs[WIDGET_STATE_KEY]?.let {
                runCatching { json.decodeFromString<WidgetStateDto>(it).toWidgetState() }.getOrNull()
            }
        }

    suspend fun updateState(state: WidgetState) {
        dataStore.edit { prefs ->
            prefs[WIDGET_STATE_KEY] = json.encodeToString(state.toDto())
        }
    }
}
