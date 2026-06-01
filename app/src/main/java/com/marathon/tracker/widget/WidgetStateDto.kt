package com.marathon.tracker.widget

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.GymSession
import com.marathon.tracker.domain.model.PaceRange
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WidgetState
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate

@Serializable
data class WidgetStateDto(
    val todayWorkout: DayWorkoutDto?,
    val lastStravaActivity: StravaActivityDto?,
    val daysToNextRace: Int,
    val nextRaceName: String,
    val weekNumber: Int,
    val weeklyKmTarget: Double,
    val weeklyKmDone: Double,
    val completedRaces: List<String>,
    val isStravaConnected: Boolean,
    val lastUpdated: Long,
    val currentPhase: String,
)

@Serializable
data class DayWorkoutDto(
    val dateEpochDay: Long,
    val weekNumber: Int,
    val dayOfWeek: Int,
    val phase: String,
    val runType: String,
    val distanceKm: Double,
    val paceMinSec: Int?,
    val paceMaxSec: Int?,
    val gymFocus: String?,
    val coachNote: String?,
    val isRaceDay: Boolean,
    val raceName: String?,
)

@Serializable
data class StravaActivityDto(
    val id: Long,
    val name: String,
    val type: String,
    val startDateEpochDay: Long,
    val distanceKm: Double,
    val movingTimeSeconds: Int,
    val averagePaceSecPerKm: Double,
    val averageHeartrate: Double?,
    val totalElevationGain: Double,
)

fun WidgetState.toDto(): WidgetStateDto = WidgetStateDto(
    todayWorkout = todayWorkout?.toDto(),
    lastStravaActivity = lastStravaActivity?.toDto(),
    daysToNextRace = daysToNextRace,
    nextRaceName = nextRaceName,
    weekNumber = weekNumber,
    weeklyKmTarget = weeklyKmTarget,
    weeklyKmDone = weeklyKmDone,
    completedRaces = completedRaces,
    isStravaConnected = isStravaConnected,
    lastUpdated = lastUpdated,
    currentPhase = currentPhase.name,
)

fun WidgetStateDto.toWidgetState(): WidgetState = WidgetState(
    todayWorkout = todayWorkout?.toDomain(),
    lastStravaActivity = lastStravaActivity?.toDomain(),
    daysToNextRace = daysToNextRace,
    nextRaceName = nextRaceName,
    weekNumber = weekNumber,
    weeklyKmTarget = weeklyKmTarget,
    weeklyKmDone = weeklyKmDone,
    completedRaces = completedRaces,
    isStravaConnected = isStravaConnected,
    lastUpdated = lastUpdated,
    currentPhase = runCatching { TrainingPhase.valueOf(currentPhase) }.getOrDefault(TrainingPhase.BASE_BUILDING),
)

fun DayWorkout.toDto(): DayWorkoutDto = DayWorkoutDto(
    dateEpochDay = date.toEpochDay(),
    weekNumber = weekNumber,
    dayOfWeek = dayOfWeek.value,
    phase = phase.name,
    runType = runType.name,
    distanceKm = distanceKm,
    paceMinSec = paceRange?.minSecondsPerKm,
    paceMaxSec = paceRange?.maxSecondsPerKm,
    gymFocus = gymSession?.focus,
    coachNote = coachNote,
    isRaceDay = isRaceDay,
    raceName = raceName,
)

fun DayWorkoutDto.toDomain(): DayWorkout = DayWorkout(
    date = LocalDate.ofEpochDay(dateEpochDay),
    weekNumber = weekNumber,
    dayOfWeek = DayOfWeek.of(dayOfWeek),
    phase = runCatching { TrainingPhase.valueOf(phase) }.getOrDefault(TrainingPhase.BASE_BUILDING),
    runType = runCatching { RunType.valueOf(runType) }.getOrDefault(RunType.EASY),
    distanceKm = distanceKm,
    paceRange = if (paceMinSec != null && paceMaxSec != null) PaceRange(paceMinSec, paceMaxSec) else null,
    gymSession = gymFocus?.let { GymSession(it, 60) },
    coachNote = coachNote,
    isRaceDay = isRaceDay,
    raceName = raceName,
)

fun StravaActivity.toDto(): StravaActivityDto = StravaActivityDto(
    id = id,
    name = name,
    type = type,
    startDateEpochDay = startDate.toEpochDay(),
    distanceKm = distanceKm,
    movingTimeSeconds = movingTimeSeconds,
    averagePaceSecPerKm = averagePaceSecPerKm,
    averageHeartrate = averageHeartrate,
    totalElevationGain = totalElevationGain,
)

fun StravaActivityDto.toDomain(): StravaActivity = StravaActivity(
    id = id,
    name = name,
    type = type,
    startDate = LocalDate.ofEpochDay(startDateEpochDay),
    distanceKm = distanceKm,
    movingTimeSeconds = movingTimeSeconds,
    averagePaceSecPerKm = averagePaceSecPerKm,
    averageHeartrate = averageHeartrate,
    maxHeartrate = null,
    totalElevationGain = totalElevationGain,
    kudosCount = 0,
    mapPolyline = null,
)
