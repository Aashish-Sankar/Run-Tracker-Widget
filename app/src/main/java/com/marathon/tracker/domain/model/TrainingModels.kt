package com.marathon.tracker.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class TrainingPhase(val displayName: String, val colorHex: Long) {
    BASE_BUILDING("Base Building", 0xFFB2DFDB),
    AEROBIC_DEVELOPMENT("Aerobic Development", 0xFFBBDEFB),
    TEMPO_INTRODUCTION("Tempo Introduction", 0xFFFFE0B2),
    RACE_PREP("Race Prep", 0xFFFFCCBC),
    PEAK_TRAINING("Peak Training", 0xFFF8BBD9),
    TAPER("Taper", 0xFFE1BEE7),
    RACE_WEEK("Race Week", 0xFFC8E6C9),
    RECOVERY("Recovery", 0xFFCFD8DC),
}

enum class RunType(val displayName: String) {
    EASY("Easy Run"),
    LONG("Long Run"),
    TEMPO("Tempo Run"),
    INTERVAL("Interval"),
    MARATHON_PACE("Marathon Pace"),
    RECOVERY_RUN("Recovery Run"),
    STRIDES("Strides"),
    RACE("Race"),
    REST("Rest Day"),
    GYM_ONLY("Gym Only"),
}

data class PaceRange(
    val minSecondsPerKm: Int,
    val maxSecondsPerKm: Int,
) {
    val midpointSecondsPerKm: Int get() = (minSecondsPerKm + maxSecondsPerKm) / 2
}

data class GymSession(
    val focus: String,
    val durationMinutes: Int,
    val notes: String? = null,
)

data class DayWorkout(
    val date: LocalDate,
    val weekNumber: Int,
    val dayOfWeek: DayOfWeek,
    val phase: TrainingPhase,
    val runType: RunType,
    val distanceKm: Double,
    val paceRange: PaceRange?,
    val gymSession: GymSession?,
    val coachNote: String?,
    val isRaceDay: Boolean = false,
    val raceName: String? = null,
)

data class WeekPlan(
    val weekNumber: Int,
    val phase: TrainingPhase,
    val startDate: LocalDate,
    val days: List<DayWorkout>,
    val totalPlannedKm: Double,
    val keyWorkoutDescription: String,
)

data class TodayWorkout(
    val plan: DayWorkout?,
    val logEntry: WorkoutLog?,
    val matchedStravaActivity: StravaActivity?,
    val isCompleted: Boolean,
)

data class WorkoutLog(
    val id: String,
    val date: LocalDate,
    val actualDistanceKm: Double?,
    val actualPaceSecPerKm: Double?,
    val stravaActivityId: Long?,
    val isCompleted: Boolean,
    val notes: String?,
)

data class WeekSummary(
    val weekNumber: Int,
    val plannedKm: Double,
    val actualKm: Double,
    val completedDays: Int,
    val totalDays: Int,
    val phase: TrainingPhase,
    val startDate: LocalDate,
)

data class StravaActivity(
    val id: Long,
    val name: String,
    val type: String,
    val startDate: LocalDate,
    val distanceKm: Double,
    val movingTimeSeconds: Int,
    val averagePaceSecPerKm: Double,
    val averageHeartrate: Double?,
    val maxHeartrate: Double?,
    val totalElevationGain: Double,
    val kudosCount: Int,
    val mapPolyline: String?,
)

data class Race(
    val name: String,
    val date: LocalDate,
    val distanceKm: Double,
    val targetFinishSeconds: Int?,
    val isCompleted: Boolean = false,
    val actualFinishSeconds: Int? = null,
)

data class AthleteInfo(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val profileUrl: String,
) {
    val fullName: String get() = "$firstName $lastName"
}

data class WidgetState(
    val todayWorkout: DayWorkout?,
    val lastStravaActivity: StravaActivity?,
    val daysToNextRace: Int,
    val nextRaceName: String,
    val weekNumber: Int,
    val weeklyKmTarget: Double,
    val weeklyKmDone: Double,
    val completedRaces: List<String>,
    val isStravaConnected: Boolean,
    val lastUpdated: Long,
    val currentPhase: TrainingPhase,
)

data class CoachingReport(
    val summary: String,
    val highlights: List<String>,
    val concerns: List<String>,
    val paceAnalysis: String,
    val nextWeekFocus: List<String>,
    val adjustments: List<String>,
    val raceReadiness: Map<String, RaceReadiness>,
    val weeklyMotivation: String,
    val generatedAt: Long,
    val weekNumber: Int,
)

data class RaceReadiness(
    val score: Int,
    val note: String,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class PaceUiState {
    FASTER_THAN_PLAN,
    ON_PLAN,
    SLIGHTLY_SLOW,
    OFF_PLAN,
}

data class PaceTrendPoint(
    val date: LocalDate,
    val paceSecPerKm: Double,
    val runType: RunType,
    val distanceKm: Double,
)

data class HrZoneData(
    val zone: Int,
    val label: String,
    val minutes: Int,
    val colorHex: Long,
)
