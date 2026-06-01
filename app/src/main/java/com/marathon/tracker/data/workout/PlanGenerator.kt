package com.marathon.tracker.data.workout

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.GymSession
import com.marathon.tracker.domain.model.PaceRange
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekPlan
import java.time.DayOfWeek
import java.time.LocalDate

object PlanGenerator {

    // Default fallback: ~6:24/km pace over 42.195km ≈ 4h30m
    private const val DEFAULT_MARATHON_SECONDS = 16200

    fun generate(
        startDate: LocalDate,
        targetMarathonSeconds: Int?,
        races: List<Race>,
    ): List<WeekPlan> {
        val marathonPaceSec = (targetMarathonSeconds ?: DEFAULT_MARATHON_SECONDS).toDouble() / 42.195

        val easyPace = PaceRange((marathonPaceSec + 90).toInt(), (marathonPaceSec + 150).toInt())
        val longRunPace = PaceRange((marathonPaceSec + 60).toInt(), (marathonPaceSec + 120).toInt())
        val tempoPace = PaceRange((marathonPaceSec - 45).toInt(), (marathonPaceSec - 10).toInt())
        val intervalPace = PaceRange((marathonPaceSec - 90).toInt(), (marathonPaceSec - 45).toInt())
        val recoveryPace = PaceRange((marathonPaceSec + 120).toInt(), (marathonPaceSec + 180).toInt())
        val marathonPace = PaceRange(marathonPaceSec.toInt(), (marathonPaceSec + 15).toInt())

        // Phase structure: weekRange -> (phase, baseVolume, keyDescription)
        data class PhaseConfig(
            val phase: TrainingPhase,
            val weekStart: Int,
            val weekEnd: Int,
            val baseVolumeKm: Double,
            val keyDesc: String,
        )

        val phaseConfigs = listOf(
            PhaseConfig(TrainingPhase.BASE_BUILDING, 1, 6, 42.0, "Easy base miles — establish routine"),
            PhaseConfig(TrainingPhase.AEROBIC_DEVELOPMENT, 7, 12, 55.0, "Build aerobic capacity with LT work"),
            PhaseConfig(TrainingPhase.TEMPO_INTRODUCTION, 13, 18, 65.0, "Introduce tempo and marathon pace runs"),
            PhaseConfig(TrainingPhase.TAPER, 19, 20, 40.0, "Mini taper before race"),
            PhaseConfig(TrainingPhase.RACE_PREP, 21, 24, 60.0, "Race-specific preparation"),
            PhaseConfig(TrainingPhase.PEAK_TRAINING, 25, 30, 70.0, "Peak training volume"),
            PhaseConfig(TrainingPhase.TAPER, 31, 33, 45.0, "Taper for marathon"),
            PhaseConfig(TrainingPhase.RECOVERY, 34, 35, 25.0, "Post-marathon recovery"),
        )

        val raceDates = races.map { it.date }.toSet()

        val weeks = mutableListOf<WeekPlan>()

        for (config in phaseConfigs) {
            for (weekNum in config.weekStart..config.weekEnd) {
                val weekOffset = weekNum - 1
                val weekStart = startDate.plusWeeks(weekOffset.toLong())

                // Volume progression within phase
                val phaseLength = config.weekEnd - config.weekStart + 1
                val phaseIndex = weekNum - config.weekStart
                val isCutback = phaseIndex == phaseLength - 1 && phaseLength >= 3

                val weekVolume = if (isCutback) {
                    config.baseVolumeKm * 0.75
                } else {
                    config.baseVolumeKm + phaseIndex * (config.baseVolumeKm * 0.05)
                }

                val days = buildWeekDays(
                    weekStart = weekStart,
                    weekNumber = weekNum,
                    phase = config.phase,
                    weekVolumeKm = weekVolume,
                    easyPace = easyPace,
                    longRunPace = longRunPace,
                    tempoPace = tempoPace,
                    intervalPace = intervalPace,
                    recoveryPace = recoveryPace,
                    marathonPace = marathonPace,
                    raceDates = raceDates,
                    races = races,
                )

                val totalKm = days.sumOf { it.distanceKm }
                val keyDesc = config.keyDesc

                weeks.add(
                    WeekPlan(
                        weekNumber = weekNum,
                        phase = config.phase,
                        startDate = weekStart,
                        days = days,
                        totalPlannedKm = totalKm,
                        keyWorkoutDescription = keyDesc,
                    )
                )
            }
        }

        return weeks
    }

    private fun buildWeekDays(
        weekStart: LocalDate,
        weekNumber: Int,
        phase: TrainingPhase,
        weekVolumeKm: Double,
        easyPace: PaceRange,
        longRunPace: PaceRange,
        tempoPace: PaceRange,
        intervalPace: PaceRange,
        recoveryPace: PaceRange,
        marathonPace: PaceRange,
        raceDates: Set<LocalDate>,
        races: List<Race>,
    ): List<DayWorkout> {
        // Distribute volume: Mon easy, Tue gym, Wed medium easy, Thu workout, Fri rest, Sat long, Sun rest
        val longFraction = when (phase) {
            TrainingPhase.BASE_BUILDING -> 0.35
            TrainingPhase.AEROBIC_DEVELOPMENT -> 0.37
            TrainingPhase.TEMPO_INTRODUCTION -> 0.35
            TrainingPhase.RACE_PREP -> 0.33
            TrainingPhase.PEAK_TRAINING -> 0.38
            TrainingPhase.TAPER -> 0.30
            TrainingPhase.RECOVERY -> 0.32
            else -> 0.35
        }

        val longKm = (weekVolumeKm * longFraction).roundToHalf()
        val monKm = (weekVolumeKm * 0.18).roundToHalf()
        val wedKm = (weekVolumeKm * 0.22).roundToHalf()
        val thuKm = (weekVolumeKm * 0.20).roundToHalf()

        val days = mutableListOf<DayWorkout>()

        for (dayIndex in 0..6) {
            val date = weekStart.plusDays(dayIndex.toLong())
            val dow = date.dayOfWeek

            // Check if this day is a race day
            val raceOnDay = if (date in raceDates) races.find { it.date == date } else null

            val workout = if (raceOnDay != null) {
                DayWorkout(
                    date = date,
                    weekNumber = weekNumber,
                    dayOfWeek = dow,
                    phase = phase,
                    runType = RunType.RACE,
                    distanceKm = raceOnDay.distanceKm,
                    paceRange = marathonPace,
                    gymSession = null,
                    coachNote = "Race day: ${raceOnDay.name}. Good luck!",
                    isRaceDay = true,
                    raceName = raceOnDay.name,
                )
            } else {
                when (dow) {
                    DayOfWeek.MONDAY -> buildRunDay(
                        date = date,
                        weekNumber = weekNumber,
                        phase = phase,
                        runType = RunType.EASY,
                        distanceKm = monKm,
                        paceRange = easyPace,
                        coachNote = "Easy aerobic run. Keep heart rate in zone 2.",
                    )
                    DayOfWeek.TUESDAY -> DayWorkout(
                        date = date,
                        weekNumber = weekNumber,
                        dayOfWeek = dow,
                        phase = phase,
                        runType = RunType.GYM_ONLY,
                        distanceKm = 0.0,
                        paceRange = null,
                        gymSession = gymSessionForPhase(phase),
                        coachNote = "Strength training day.",
                        isRaceDay = false,
                        raceName = null,
                    )
                    DayOfWeek.WEDNESDAY -> buildRunDay(
                        date = date,
                        weekNumber = weekNumber,
                        phase = phase,
                        runType = RunType.EASY,
                        distanceKm = wedKm,
                        paceRange = easyPace,
                        coachNote = "Comfortable medium effort. Build the aerobic base.",
                    )
                    DayOfWeek.THURSDAY -> buildQualityDay(
                        date = date,
                        weekNumber = weekNumber,
                        phase = phase,
                        distanceKm = thuKm,
                        tempoPace = tempoPace,
                        intervalPace = intervalPace,
                        marathonPace = marathonPace,
                        recoveryPace = recoveryPace,
                    )
                    DayOfWeek.FRIDAY -> DayWorkout(
                        date = date,
                        weekNumber = weekNumber,
                        dayOfWeek = dow,
                        phase = phase,
                        runType = RunType.REST,
                        distanceKm = 0.0,
                        paceRange = null,
                        gymSession = null,
                        coachNote = "Rest day. Recover before the long run.",
                        isRaceDay = false,
                        raceName = null,
                    )
                    DayOfWeek.SATURDAY -> buildRunDay(
                        date = date,
                        weekNumber = weekNumber,
                        phase = phase,
                        runType = RunType.LONG,
                        distanceKm = longKm,
                        paceRange = longRunPace,
                        coachNote = longRunNote(phase, longKm),
                    )
                    DayOfWeek.SUNDAY -> DayWorkout(
                        date = date,
                        weekNumber = weekNumber,
                        dayOfWeek = dow,
                        phase = phase,
                        runType = RunType.REST,
                        distanceKm = 0.0,
                        paceRange = null,
                        gymSession = null,
                        coachNote = "Full rest day. Recovery is when you improve.",
                        isRaceDay = false,
                        raceName = null,
                    )
                }
            }

            days.add(workout)
        }

        return days
    }

    private fun buildRunDay(
        date: LocalDate,
        weekNumber: Int,
        phase: TrainingPhase,
        runType: RunType,
        distanceKm: Double,
        paceRange: PaceRange,
        coachNote: String,
    ) = DayWorkout(
        date = date,
        weekNumber = weekNumber,
        dayOfWeek = date.dayOfWeek,
        phase = phase,
        runType = runType,
        distanceKm = distanceKm,
        paceRange = paceRange,
        gymSession = null,
        coachNote = coachNote,
        isRaceDay = false,
        raceName = null,
    )

    private fun buildQualityDay(
        date: LocalDate,
        weekNumber: Int,
        phase: TrainingPhase,
        distanceKm: Double,
        tempoPace: PaceRange,
        intervalPace: PaceRange,
        marathonPace: PaceRange,
        recoveryPace: PaceRange,
    ): DayWorkout {
        val (runType, pace, note) = when (phase) {
            TrainingPhase.BASE_BUILDING -> Triple(
                RunType.EASY,
                tempoPace,
                "Easy run with optional strides at the end.",
            )
            TrainingPhase.AEROBIC_DEVELOPMENT -> Triple(
                RunType.INTERVAL,
                intervalPace,
                "Interval workout. Warm up 2km, 5×1km at interval pace with 90s rest, cool down 2km.",
            )
            TrainingPhase.TEMPO_INTRODUCTION -> Triple(
                RunType.TEMPO,
                tempoPace,
                "Tempo run. Warm up 2km, tempo block, cool down 2km. Comfortably hard effort.",
            )
            TrainingPhase.RACE_PREP -> Triple(
                RunType.MARATHON_PACE,
                marathonPace,
                "Marathon pace run. Target goal race pace for the middle portion.",
            )
            TrainingPhase.PEAK_TRAINING -> Triple(
                RunType.TEMPO,
                tempoPace,
                "Tempo or marathon pace. Strong but controlled effort.",
            )
            TrainingPhase.TAPER -> Triple(
                RunType.EASY,
                recoveryPace,
                "Easy effort. Keep legs fresh for the race.",
            )
            TrainingPhase.RECOVERY -> Triple(
                RunType.RECOVERY_RUN,
                recoveryPace,
                "Very easy recovery run. Stay relaxed.",
            )
            else -> Triple(
                RunType.EASY,
                tempoPace,
                "Quality workout day.",
            )
        }

        return DayWorkout(
            date = date,
            weekNumber = weekNumber,
            dayOfWeek = date.dayOfWeek,
            phase = phase,
            runType = runType,
            distanceKm = distanceKm,
            paceRange = pace,
            gymSession = null,
            coachNote = note,
            isRaceDay = false,
            raceName = null,
        )
    }

    private fun gymSessionForPhase(phase: TrainingPhase): GymSession = when (phase) {
        TrainingPhase.BASE_BUILDING -> GymSession("Strength + Core", 60)
        TrainingPhase.AEROBIC_DEVELOPMENT -> GymSession("Legs + Core", 60)
        TrainingPhase.TEMPO_INTRODUCTION -> GymSession("Full Body", 60)
        TrainingPhase.RACE_PREP -> GymSession("Upper Body + Core", 45)
        TrainingPhase.PEAK_TRAINING -> GymSession("Maintenance Strength", 45)
        TrainingPhase.TAPER -> GymSession("Mobility + Light Core", 30)
        TrainingPhase.RECOVERY -> GymSession("Yoga + Mobility", 30)
        else -> GymSession("Strength", 45)
    }

    private fun longRunNote(phase: TrainingPhase, distanceKm: Double): String = when (phase) {
        TrainingPhase.BASE_BUILDING -> "Long run — stay in zone 2 the entire way. ${distanceKm.toInt()}km."
        TrainingPhase.AEROBIC_DEVELOPMENT -> "Long run at easy pace. Practice race nutrition. ${distanceKm.toInt()}km."
        TrainingPhase.TEMPO_INTRODUCTION -> "Long run with last portion at marathon pace. ${distanceKm.toInt()}km."
        TrainingPhase.RACE_PREP -> "Race simulation long run. ${distanceKm.toInt()}km at target pace."
        TrainingPhase.PEAK_TRAINING -> "Peak long run. Fuel well and stay strong. ${distanceKm.toInt()}km."
        TrainingPhase.TAPER -> "Shortened long run. Keep effort easy. ${distanceKm.toInt()}km."
        TrainingPhase.RECOVERY -> "Very easy recovery long run. ${distanceKm.toInt()}km."
        else -> "Long run. ${distanceKm.toInt()}km."
    }

    private fun Double.roundToHalf(): Double = Math.round(this * 2).toDouble() / 2
}
