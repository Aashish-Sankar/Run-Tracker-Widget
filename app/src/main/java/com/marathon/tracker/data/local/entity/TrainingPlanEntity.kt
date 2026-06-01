package com.marathon.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_plans")
data class TrainingPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startDateEpochDay: Long,
    val targetMarathonSeconds: Int?,
    val racesJson: String,
    val weeksJson: String,
    val isActive: Boolean,
    val isDefault: Boolean,
    val createdAtMillis: Long,
)
