package com.marathon.tracker.data.local.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainingPlanDto(
    val name: String,
    val startDate: String,
    val targetMarathonSeconds: Int? = null,
    val races: List<RaceDto> = emptyList(),
    val weeks: List<WeekPlanDto>,
)

@Serializable
data class RaceDto(
    val name: String,
    val date: String,
    val distanceKm: Double,
    val targetFinishSeconds: Int? = null,
)

@Serializable
data class WeekPlanDto(
    val weekNumber: Int,
    val phase: String,
    val keyWorkoutDescription: String = "",
    val days: List<DayWorkoutDto>,
)

@Serializable
data class DayWorkoutDto(
    val date: String,
    val dayOfWeek: String,
    val runType: String,
    val distanceKm: Double = 0.0,
    val paceMinSecondsPerKm: Int? = null,
    val paceMaxSecondsPerKm: Int? = null,
    val gymSessionFocus: String? = null,
    val gymSessionDurationMinutes: Int? = null,
    val coachNote: String? = null,
    val isRaceDay: Boolean = false,
    val raceName: String? = null,
)
