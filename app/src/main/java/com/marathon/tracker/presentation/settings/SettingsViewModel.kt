package com.marathon.tracker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.auth.StravaAuthManager
import com.marathon.tracker.auth.TokenManager
import com.marathon.tracker.domain.model.ThemeMode
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val tokenManager: TokenManager,
    private val stravaAuthManager: StravaAuthManager,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        preferencesRepository.getThemeMode()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val widgetRefreshInterval: StateFlow<Int> =
        preferencesRepository.getRefreshIntervalMinutes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val isStravaConnected: StateFlow<Boolean> =
        preferencesRepository.getAthleteInfo()
            .map { it != null && tokenManager.hasValidTokens() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val athleteName: StateFlow<String> =
        preferencesRepository.getAthleteInfo()
            .map { it?.fullName ?: "" }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val athleteAvatarUrl: StateFlow<String> =
        preferencesRepository.getAthleteInfo()
            .map { it?.profileUrl ?: "" }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setRefreshInterval(minutes: Int) {
        viewModelScope.launch { preferencesRepository.setRefreshIntervalMinutes(minutes) }
    }

    fun launchStravaAuth(activity: androidx.activity.ComponentActivity) {
        stravaAuthManager.launchAuthFlow(activity)
    }

    fun disconnectStrava() {
        stravaAuthManager.disconnect()
    }

    fun saveClaudeApiKey(key: String) {
        tokenManager.saveClaudeApiKey(key)
    }

    fun getClaudeApiKey(): String = tokenManager.getClaudeApiKey() ?: ""
}
