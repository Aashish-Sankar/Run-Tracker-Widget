package com.marathon.tracker.domain.model

import java.time.LocalDate

data class TrainingPlan(
    val id: String,
    val name: String,
    val startDate: LocalDate,
    val targetMarathonSeconds: Int?,
    val races: List<Race>,
    val weeks: List<WeekPlan>,
    val isDefault: Boolean,
    val createdAtMillis: Long,
)
