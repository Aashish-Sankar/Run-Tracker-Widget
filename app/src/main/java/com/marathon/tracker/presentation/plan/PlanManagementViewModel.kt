package com.marathon.tracker.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.TrainingPlan
import com.marathon.tracker.domain.repository.PlanRepository
import com.marathon.tracker.util.PlanJsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanManagementViewModel @Inject constructor(
    private val planRepository: PlanRepository,
) : ViewModel() {

    val plans: StateFlow<List<TrainingPlan>> = planRepository.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlanId: StateFlow<String?> = planRepository.observeActivePlan()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun activatePlan(planId: String) {
        viewModelScope.launch {
            planRepository.activatePlan(planId)
        }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            planRepository.deletePlan(planId)
        }
    }

    fun importPlan(jsonString: String) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = PlanJsonParser.parse(jsonString)
            result.fold(
                onSuccess = { dto ->
                    try {
                        val newPlan = planRepository.importPlan(dto)
                        planRepository.activatePlan(newPlan.id)
                        _events.emit("Plan '${newPlan.name}' imported and activated.")
                    } catch (e: Exception) {
                        _events.emit("Import failed: ${e.message ?: "Unknown error"}")
                    }
                },
                onFailure = { e ->
                    _events.emit("Invalid JSON: ${e.message ?: "Unknown error"}")
                },
            )
            _isImporting.value = false
        }
    }
}
