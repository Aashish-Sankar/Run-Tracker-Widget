package com.marathon.tracker.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.repository.StravaRepository
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import com.marathon.tracker.domain.usecase.SyncStravaActivitiesUseCase
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
class ActivitiesViewModel @Inject constructor(
    private val stravaRepository: StravaRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val syncStravaActivitiesUseCase: SyncStravaActivitiesUseCase,
) : ViewModel() {

    val activities: StateFlow<List<StravaActivity>> =
        stravaRepository.getRecentActivities(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastSyncTime: StateFlow<Long> =
        preferencesRepository.getLastSyncTimeMillis()
            .map { it ?: 0L }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun sync() {
        viewModelScope.launch {
            _isSyncing.update { true }
            syncStravaActivitiesUseCase()
            _isSyncing.update { false }
        }
    }
}
