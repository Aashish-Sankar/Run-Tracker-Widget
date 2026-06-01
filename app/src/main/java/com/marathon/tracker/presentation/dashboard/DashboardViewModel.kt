package com.marathon.tracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.HrZoneData
import com.marathon.tracker.domain.model.PaceTrendPoint
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.model.WeekSummary
import com.marathon.tracker.domain.repository.PlanRepository
import com.marathon.tracker.domain.repository.StravaRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import com.marathon.tracker.domain.usecase.GetTodayWorkoutUseCase
import com.marathon.tracker.domain.usecase.SyncStravaActivitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val stravaRepository: StravaRepository,
    private val planRepository: PlanRepository,
    private val getTodayWorkoutUseCase: GetTodayWorkoutUseCase,
    private val syncStravaActivitiesUseCase: SyncStravaActivitiesUseCase,
) : ViewModel() {

    val weekSummary: StateFlow<WeekSummary?> =
        workoutRepository.getWeekSummary(workoutRepository.getCurrentWeekNumber())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentActivities: StateFlow<List<StravaActivity>> =
        stravaRepository.getRecentActivities(14)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWorkout: StateFlow<TodayWorkout?> =
        getTodayWorkoutUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val upcomingRaces: StateFlow<List<Race>> =
        planRepository.observeActivePlan()
            .map { plan ->
                val today = LocalDate.now()
                (plan?.races ?: emptyList()).filter { it.date >= today }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paceTrendData: StateFlow<List<PaceTrendPoint>> =
        stravaRepository.getRecentActivities(8)
            .map { activities ->
                activities.filter { it.type == "Run" && it.averagePaceSecPerKm > 0 }
                    .map { PaceTrendPoint(it.startDate, it.averagePaceSecPerKm, com.marathon.tracker.domain.model.RunType.EASY, it.distanceKm) }
                    .reversed()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hrZoneData: StateFlow<List<HrZoneData>> =
        stravaRepository.getRecentActivities(7)
            .map { activities ->
                // Simplified HR zone distribution based on average HR
                val runsWithHr = activities.filter { it.averageHeartrate != null }
                if (runsWithHr.isEmpty()) return@map defaultHrZones()

                val avgHr = runsWithHr.mapNotNull { it.averageHeartrate }.average()
                buildHrZones(avgHr)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultHrZones())

    fun markCompleted(actualKm: Double, actualPaceSecPerKm: Double) {
        viewModelScope.launch {
            workoutRepository.markWorkoutCompleted(LocalDate.now(), actualKm, actualPaceSecPerKm)
        }
    }

    fun syncStrava() {
        viewModelScope.launch {
            _isSyncing.update { true }
            syncStravaActivitiesUseCase()
            _isSyncing.update { false }
        }
    }

    private fun defaultHrZones() = listOf(
        HrZoneData(1, "Zone 1", 30, 0xFF4CAF50),
        HrZoneData(2, "Zone 2", 60, 0xFF8BC34A),
        HrZoneData(3, "Zone 3", 45, 0xFFFFC107),
        HrZoneData(4, "Zone 4", 20, 0xFFFF5722),
        HrZoneData(5, "Zone 5", 5, 0xFFF44336),
    )

    private fun buildHrZones(avgHr: Double): List<HrZoneData> {
        val maxHr = 190.0
        return when {
            avgHr < maxHr * 0.60 -> defaultHrZones().map { if (it.zone == 1) it.copy(minutes = 90) else it.copy(minutes = it.minutes / 2) }
            avgHr < maxHr * 0.70 -> defaultHrZones().map { if (it.zone == 2) it.copy(minutes = 90) else it.copy(minutes = it.minutes / 2) }
            avgHr < maxHr * 0.80 -> defaultHrZones().map { if (it.zone == 3) it.copy(minutes = 90) else it.copy(minutes = it.minutes / 2) }
            else -> defaultHrZones()
        }
    }
}
