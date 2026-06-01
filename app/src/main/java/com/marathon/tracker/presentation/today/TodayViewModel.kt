package com.marathon.tracker.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.repository.WorkoutRepository
import com.marathon.tracker.domain.usecase.GetTodayWorkoutUseCase
import com.marathon.tracker.domain.usecase.SyncStravaActivitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Success(
        val todayWorkout: TodayWorkout,
        val isSyncing: Boolean = false,
    ) : TodayUiState
    data class Error(val message: String) : TodayUiState
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayWorkoutUseCase: GetTodayWorkoutUseCase,
    private val workoutRepository: WorkoutRepository,
    private val syncStravaActivitiesUseCase: SyncStravaActivitiesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTodayWorkoutUseCase().collect { todayWorkout ->
                _uiState.value = TodayUiState.Success(todayWorkout)
            }
        }
    }

    fun markCompleted(actualKm: Double, actualPaceSecPerKm: Double) {
        viewModelScope.launch {
            workoutRepository.markWorkoutCompleted(LocalDate.now(), actualKm, actualPaceSecPerKm)
        }
    }

    fun syncStrava() {
        viewModelScope.launch {
            _uiState.update { if (it is TodayUiState.Success) it.copy(isSyncing = true) else it }
            syncStravaActivitiesUseCase()
            _uiState.update { if (it is TodayUiState.Success) it.copy(isSyncing = false) else it }
        }
    }
}
