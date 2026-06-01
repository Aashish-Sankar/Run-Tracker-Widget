package com.marathon.tracker.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.repository.PlanRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class PlanViewMode { LIST, CALENDAR }
enum class CalendarScale { DAY, WEEK, MONTH }

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val planRepository: PlanRepository,
) : ViewModel() {

    val currentWeekNumber: Int = workoutRepository.getCurrentWeekNumber()

    val activePlanName: StateFlow<String> = planRepository.observeActivePlan()
        .map { it?.name ?: "Training Plan" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Training Plan")

    private val allWeeksFromPlan: StateFlow<List<WeekPlan>> = planRepository.observeActivePlan()
        .map { plan -> plan?.weeks ?: workoutRepository.getAllWeeks() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), workoutRepository.getAllWeeks())

    val dayWorkoutMap: StateFlow<Map<LocalDate, DayWorkout>> = allWeeksFromPlan
        .map { weeks -> weeks.flatMap { it.days }.associateBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _selectedPhase = MutableStateFlow<TrainingPhase?>(null)
    val selectedPhase: StateFlow<TrainingPhase?> = _selectedPhase

    val allWeeks: StateFlow<List<WeekPlan>> = combine(_selectedPhase, allWeeksFromPlan) { phase, weeks ->
        if (phase == null) weeks else weeks.filter { it.phase == phase }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), workoutRepository.getAllWeeks())

    // Calendar state
    private val _viewMode = MutableStateFlow(PlanViewMode.LIST)
    val viewMode: StateFlow<PlanViewMode> = _viewMode

    private val _calendarScale = MutableStateFlow(CalendarScale.MONTH)
    val calendarScale: StateFlow<CalendarScale> = _calendarScale

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    val calendarMonth: StateFlow<YearMonth> = _calendarMonth

    fun selectPhase(phase: TrainingPhase?) { _selectedPhase.value = phase }
    fun setViewMode(mode: PlanViewMode) { _viewMode.value = mode }
    fun setCalendarScale(scale: CalendarScale) { _calendarScale.value = scale }
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _calendarMonth.value = YearMonth.from(date)
    }
    fun navigateMonth(forward: Boolean) {
        _calendarMonth.update { if (forward) it.plusMonths(1) else it.minusMonths(1) }
    }
}
