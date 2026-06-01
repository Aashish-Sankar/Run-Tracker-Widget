package com.marathon.tracker.util

import com.marathon.tracker.data.local.dto.TrainingPlanDto
import kotlinx.serialization.json.Json
import java.time.LocalDate

object PlanJsonParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonString: String): Result<TrainingPlanDto> {
        return try {
            val dto = json.decodeFromString<TrainingPlanDto>(jsonString)

            if (dto.weeks.isEmpty()) {
                return Result.failure(IllegalArgumentException("Plan must contain at least one week"))
            }

            // Validate startDate is a parseable ISO LocalDate
            try {
                LocalDate.parse(dto.startDate)
            } catch (e: Exception) {
                return Result.failure(IllegalArgumentException("Invalid startDate '${dto.startDate}': must be ISO format yyyy-MM-dd"))
            }

            Result.success(dto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
