package com.marathon.tracker.presentation.coaching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.domain.model.CoachingReport
import com.marathon.tracker.domain.repository.CoachingRepository
import com.marathon.tracker.domain.usecase.GenerateCoachingReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val coachingRepository: CoachingRepository,
    private val generateCoachingReportUseCase: GenerateCoachingReportUseCase,
) : ViewModel() {

    val report: StateFlow<CoachingReport?> =
        coachingRepository.getLatestReport()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val canGenerate: StateFlow<Boolean> =
        coachingRepository.canGenerateReport()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun generateReport() {
        viewModelScope.launch {
            _isGenerating.update { true }
            _error.update { null }
            val result = generateCoachingReportUseCase()
            result.onFailure { _error.update { it.message } }
            _isGenerating.update { false }
        }
    }
}
