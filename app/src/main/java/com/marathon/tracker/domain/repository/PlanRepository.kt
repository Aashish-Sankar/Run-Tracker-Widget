package com.marathon.tracker.domain.repository

import com.marathon.tracker.data.local.dto.TrainingPlanDto
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.TrainingPlan
import com.marathon.tracker.domain.model.WeekPlan
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PlanRepository {
    fun getAllPlans(): Flow<List<TrainingPlan>>
    fun observeActivePlan(): Flow<TrainingPlan?>
    suspend fun getActivePlan(): TrainingPlan?
    suspend fun activatePlan(planId: String)
    suspend fun importPlan(planDto: TrainingPlanDto): TrainingPlan
    suspend fun deletePlan(planId: String)
    suspend fun createPlan(
        name: String,
        startDate: LocalDate,
        targetMarathonSeconds: Int?,
        races: List<Race>,
    ): TrainingPlan
    suspend fun seedDefaultIfNeeded(defaultWeeks: List<WeekPlan>, defaultRaces: List<Race>)
}
