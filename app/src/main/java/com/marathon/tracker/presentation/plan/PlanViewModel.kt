package com.marathon.tracker.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    val currentWeekNumber: Int = workoutRepository.getCurrentWeekNumber()

    private val allWeeksFlow = flow { emit(workoutRepository.getAllWeeks()) }

    private val _selectedPhase = MutableStateFlow<TrainingPhase?>(null)
    val selectedPhase: StateFlow<TrainingPhase?> = _selectedPhase

    val allWeeks: StateFlow<List<WeekPlan>> = combine(
        _selectedPhase,
        allWeeksFlow,
    ) { phase, weeks ->
        if (phase == null) weeks else weeks.filter { it.phase == phase }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), workoutRepository.getAllWeeks())

    fun selectPhase(phase: TrainingPhase?) {
        _selectedPhase.value = phase
    }
}
