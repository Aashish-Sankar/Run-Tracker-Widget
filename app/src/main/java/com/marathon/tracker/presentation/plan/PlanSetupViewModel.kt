package com.marathon.tracker.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RaceEntry(
    val name: String,
    val date: LocalDate,
    val distanceKm: Double,
)

@HiltViewModel
class PlanSetupViewModel @Inject constructor(
    private val planRepository: PlanRepository,
) : ViewModel() {

    val name = MutableStateFlow("")
    val startDate = MutableStateFlow(LocalDate.now())
    val targetTimeText = MutableStateFlow("3:45:00")
    val races = MutableStateFlow<List<RaceEntry>>(emptyList())
    val isSaving = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    fun addRace() {
        races.update { current ->
            current + RaceEntry(
                name = "Race ${current.size + 1}",
                date = startDate.value.plusWeeks(20),
                distanceKm = 42.195,
            )
        }
    }

    fun updateRace(index: Int, entry: RaceEntry) {
        races.update { current ->
            current.toMutableList().also { it[index] = entry }
        }
    }

    fun removeRace(index: Int) {
        races.update { current ->
            current.toMutableList().also { it.removeAt(index) }
        }
    }

    fun save() {
        viewModelScope.launch {
            error.value = null
            val planName = name.value.trim()
            if (planName.isBlank()) {
                error.value = "Plan name cannot be empty"
                return@launch
            }

            val targetSeconds = parseTimeToSeconds(targetTimeText.value)
            if (targetTimeText.value.isNotBlank() && targetSeconds == null) {
                error.value = "Invalid time format. Use H:MM:SS or M:SS"
                return@launch
            }

            val domainRaces = races.value.map { entry ->
                Race(
                    name = entry.name,
                    date = entry.date,
                    distanceKm = entry.distanceKm,
                    targetFinishSeconds = targetSeconds,
                )
            }

            isSaving.value = true
            try {
                val newPlan = planRepository.createPlan(
                    name = planName,
                    startDate = startDate.value,
                    targetMarathonSeconds = targetSeconds,
                    races = domainRaces,
                )
                planRepository.activatePlan(newPlan.id)
                _saved.emit(Unit)
            } catch (e: Exception) {
                error.value = "Failed to save plan: ${e.message ?: "Unknown error"}"
            } finally {
                isSaving.value = false
            }
        }
    }

    private fun parseTimeToSeconds(text: String): Int? {
        if (text.isBlank()) return null
        val parts = text.trim().split(":")
        return try {
            when (parts.size) {
                3 -> {
                    val h = parts[0].toInt()
                    val m = parts[1].toInt()
                    val s = parts[2].toInt()
                    h * 3600 + m * 60 + s
                }
                2 -> {
                    val m = parts[0].toInt()
                    val s = parts[1].toInt()
                    m * 60 + s
                }
                else -> null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
