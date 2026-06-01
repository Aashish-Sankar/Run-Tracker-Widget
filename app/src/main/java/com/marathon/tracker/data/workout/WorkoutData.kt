package com.marathon.tracker.data.workout

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.GymSession
import com.marathon.tracker.domain.model.PaceRange
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.TrainingPaces
import com.marathon.tracker.domain.model.WeekPlan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object WorkoutData {

    val PLAN_START_DATE: LocalDate = LocalDate.of(2026, 6, 1)
    val PLAN_END_DATE: LocalDate = LocalDate.of(2027, 1, 31)

    val RACES: List<Race> = listOf(
        Race("Half Marathon", LocalDate.of(2026, 10, 18), 21.1, 2 * 3600 + 15 * 60),
        Race("25K Race", LocalDate.of(2026, 12, 20), 25.0, 2 * 3600 + 45 * 60),
        Race("Full Marathon", LocalDate.of(2027, 1, 17), 42.195, 4 * 3600 + 30 * 60),
    )

    val ALL_WEEKS: List<WeekPlan> by lazy {
        buildList {
            // BASE BUILDING: Weeks 1–6 (Jun 1 – Jul 12)
            add(buildWeek(1, TrainingPhase.BASE_BUILDING, 42.0, "Easy base miles — establish routine",
                listOf(8.0, 0.0, 10.0, 8.0, 0.0, 16.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength + Core", 60), null, null, null, null, null),
                listOf("First run of the plan. Keep it easy.", null, "Build the aerobic base.", "Easy effort, nose breathing.", "Rest day. Recover.", "Stay in zone 2 the whole way.", "Full rest day.")))
            add(buildWeek(2, TrainingPhase.BASE_BUILDING, 45.0, "Zone 2 focus with first strides",
                listOf(8.0, 0.0, 10.0, 8.0, 0.0, 18.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Legs + Core", 60), null, null, null, null, null),
                listOf("Zone 2 only.", null, "Comfortable effort.", "Add 4×100m strides at end.", null, "Longest run so far. Fuel well.", "Rest.")))
            add(buildWeek(3, TrainingPhase.BASE_BUILDING, 48.0, "Aerobic base with light tempo intro",
                listOf(8.0, 0.0, 11.0, 9.0, 0.0, 19.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Upper + Core", 60), null, null, null, null, null),
                listOf("Keep heart rate low.", null, "Easy aerobic.", "2km warm-up, 3km at tempo, 2km cool-down.", null, "Nutrition practice run.", "Rest.")))
            add(buildWeek(4, TrainingPhase.BASE_BUILDING, 50.0, "Build weekly volume",
                listOf(10.0, 0.0, 11.0, 9.0, 0.0, 20.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Full Body", 60), null, null, null, null, null),
                listOf("Aerobic foundation.", null, "Easy effort.", "Warm-up + 4km tempo + cool-down.", null, "First 20km long run!", "Recover.")))
            add(buildWeek(5, TrainingPhase.BASE_BUILDING, 52.0, "Consistency week",
                listOf(10.0, 0.0, 12.0, 10.0, 0.0, 20.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Legs + Core", 60), null, null, null, null, null),
                listOf("Maintain easy effort.", null, "Feel the aerobic base growing.", "5km tempo section.", null, "Steady zone 2 long run.", "Full rest.")))
            add(buildWeek(6, TrainingPhase.BASE_BUILDING, 48.0, "Cutback/recovery week",
                listOf(8.0, 0.0, 10.0, 8.0, 0.0, 14.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.RECOVERY_RUN, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.RECOVERY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Mobility + Core", 45), null, null, null, null, null),
                listOf("Recovery week. Back off the effort.", null, "Very easy — 70% max HR only.", "Light effort.", null, "Easy long run — shorter this week.", "Rest and recover.")))

            // AEROBIC DEVELOPMENT: Weeks 7–12 (Jul 13 – Aug 23)
            add(buildWeek(7, TrainingPhase.AEROBIC_DEVELOPMENT, 55.0, "Increase volume and add LT intervals",
                listOf(10.0, 0.0, 13.0, 12.0, 0.0, 20.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.INTERVAL, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.SPEED_REPS, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength", 60), null, null, null, null, null),
                listOf("New phase begins — aerobic development.", null, "Build volume.", "5×1km at 5K pace with 90s rest.", null, "21km long run. Good effort.", "Rest day.")))
            add(buildWeek(8, TrainingPhase.AEROBIC_DEVELOPMENT, 58.0, "LT work + weekend long run",
                listOf(10.0, 0.0, 13.0, 12.0, 0.0, 23.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Legs + Core", 60), null, null, null, null, null),
                listOf("Strong aerobic base.", null, "Easy effort.", "6km tempo block.", null, "First 23km run!", "Recover.")))
            add(buildWeek(9, TrainingPhase.AEROBIC_DEVELOPMENT, 60.0, "Solid training week",
                listOf(12.0, 0.0, 13.0, 12.0, 0.0, 23.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.INTERVAL, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.SPEED_REPS, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Full Body", 60), null, null, null, null, null),
                listOf("Easy aerobic.", null, "Comfortable pace.", "6×800m at 10K effort.", null, "Stay in zone 2.", "Full rest.")))
            add(buildWeek(10, TrainingPhase.AEROBIC_DEVELOPMENT, 62.0, "Progressive long run introduced",
                listOf(12.0, 0.0, 14.0, 13.0, 0.0, 24.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Core + Mobility", 45), null, null, null, null, null),
                listOf("Consistent aerobic base.", null, "Easy run.", "12km with last 5km at marathon pace.", null, "24km — practice race nutrition.", "Rest.")))
            add(buildWeek(11, TrainingPhase.AEROBIC_DEVELOPMENT, 58.0, "Cutback week",
                listOf(10.0, 0.0, 12.0, 10.0, 0.0, 18.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.RECOVERY_RUN, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.RECOVERY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Mobility", 45), null, null, null, null, null),
                listOf("Recovery week. Back off.", null, "Very easy.", "Easy effort only.", null, "Relaxed long run.", "Rest.")))
            add(buildWeek(12, TrainingPhase.AEROBIC_DEVELOPMENT, 64.0, "Peak aerobic development week",
                listOf(12.0, 0.0, 14.0, 13.0, 0.0, 25.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength", 60), null, null, null, null, null),
                listOf("Strong week ahead.", null, "Easy aerobic.", "7km tempo block.", null, "25km — great milestone!", "Recover.")))

            // TEMPO INTRODUCTION: Weeks 13–18 (Aug 24 – Oct 4)
            add(buildWeek(13, TrainingPhase.TEMPO_INTRODUCTION, 65.0, "Dedicated tempo sessions begin",
                listOf(12.0, 0.0, 14.0, 13.0, 0.0, 26.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Legs + Core", 60), null, null, null, null, null),
                listOf("New phase. Tempo effort matters now.", null, "Easy recovery.", "8km at LT pace — controlled effort.", null, "26km — longest yet.", "Full rest.")))
            add(buildWeek(14, TrainingPhase.TEMPO_INTRODUCTION, 67.0, "Half marathon pace work",
                listOf(12.0, 0.0, 14.0, 14.0, 0.0, 27.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.HM_RACE, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Upper + Core", 60), null, null, null, null, null),
                listOf("Focus on efficiency.", null, "Easy aerobic.", "2×4km at HM pace with 3min rest.", null, "27km easy-steady.", "Rest.")))
            add(buildWeek(15, TrainingPhase.TEMPO_INTRODUCTION, 68.0, "Build HM-specific fitness",
                listOf(12.0, 0.0, 14.0, 14.0, 0.0, 28.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.HM_RACE, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Full Body", 60), null, null, null, null, null),
                listOf("Another solid week.", null, "Easy.", "3×3km at HM pace.", null, "28km — fuel every 45min.", "Rest.")))
            add(buildWeek(16, TrainingPhase.TEMPO_INTRODUCTION, 60.0, "Cutback week",
                listOf(10.0, 0.0, 12.0, 10.0, 0.0, 20.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.RECOVERY_RUN, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.RECOVERY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Mobility + Core", 45), null, null, null, null, null),
                listOf("Cutback week. Let the body adapt.", null, "Very easy effort.", "Easy strides at end.", null, "Relaxed 20km.", "Rest.")))
            add(buildWeek(17, TrainingPhase.TEMPO_INTRODUCTION, 70.0, "Peak tempo week before HM taper",
                listOf(12.0, 0.0, 15.0, 14.0, 0.0, 29.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.TEMPO, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength", 60), null, null, null, null, null),
                listOf("Biggest week before HM. You're ready.", null, "Easy aerobic.", "9km tempo block at HM effort.", null, "29km — last big long run before HM taper.", "Recover well.")))
            add(buildWeek(18, TrainingPhase.TEMPO_INTRODUCTION, 65.0, "Tune-up week",
                listOf(12.0, 0.0, 14.0, 12.0, 0.0, 26.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.HM_RACE, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Light Strength", 45), null, null, null, null, null),
                listOf("Tune-up week.", null, "Easy.", "4×2km at goal HM pace.", null, "26km at steady effort.", "Rest.")))

            // TAPER for HM: Weeks 19–20 (Oct 5–18)
            add(buildWeek(19, TrainingPhase.TAPER, 45.0, "HM taper — reduce volume, keep intensity",
                listOf(10.0, 0.0, 10.0, 8.0, 0.0, 10.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.EASY, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.HM_RACE, null, TrainingPaces.EASY, null),
                listOf(null, GymSession("Light Mobility", 30), null, null, null, null, null),
                listOf("Taper begins. Less volume, same sharpness.", null, "Easy.", "3km at race pace to stay sharp.", null, "Easy shakeout.", "Rest. Race week is next.")))
            add(buildWeekWithRaceDay(20, TrainingPhase.RACE_WEEK, 32.0, "HM RACE WEEK — Oct 18",
                LocalDate.of(2026, 10, 12),
                mapOf(
                    DayOfWeek.MONDAY to Triple(8.0, RunType.EASY, TrainingPaces.EASY),
                    DayOfWeek.TUESDAY to Triple(0.0, RunType.GYM_ONLY, null),
                    DayOfWeek.WEDNESDAY to Triple(6.0, RunType.EASY, TrainingPaces.EASY),
                    DayOfWeek.THURSDAY to Triple(5.0, RunType.STRIDES, TrainingPaces.STRIDES),
                    DayOfWeek.FRIDAY to Triple(0.0, RunType.REST, null),
                    DayOfWeek.SATURDAY to Triple(3.0, RunType.RECOVERY_RUN, TrainingPaces.RECOVERY),
                    DayOfWeek.SUNDAY to Triple(21.1, RunType.RACE, TrainingPaces.HM_RACE),
                ),
                mapOf(
                    DayOfWeek.MONDAY to "Last solid easy run before race week.",
                    DayOfWeek.TUESDAY to "Light gym — activation only.",
                    DayOfWeek.WEDNESDAY to "Short easy run. Stay fresh.",
                    DayOfWeek.THURSDAY to "Strides to stay sharp. 6×100m.",
                    DayOfWeek.FRIDAY to "REST. Carb-load and stay off your feet.",
                    DayOfWeek.SATURDAY to "Easy 3km shakeout — just to move.",
                    DayOfWeek.SUNDAY to "RACE DAY — Half Marathon! Target: sub-2:15.",
                ),
                "RACE",
                "Half Marathon"))

            // RACE PREP: Weeks 21–24 (Oct 19 – Nov 15) — post HM, building to 25K
            add(buildWeek(21, TrainingPhase.RACE_PREP, 50.0, "Post-HM recovery and rebuild",
                listOf(8.0, 0.0, 10.0, 10.0, 0.0, 20.0, 0.0),
                listOf(RunType.RECOVERY_RUN, RunType.GYM_ONLY, RunType.EASY, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.RECOVERY, null, TrainingPaces.EASY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Active Recovery", 30), null, null, null, null, null),
                listOf("Post-race recovery. Take it easy.", null, "Light aerobic.", "Back to easy running.", null, "20km — back in the groove.", "Rest.")))
            add(buildWeek(22, TrainingPhase.RACE_PREP, 60.0, "Rebuild to marathon-specific work",
                listOf(12.0, 0.0, 13.0, 12.0, 0.0, 23.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength", 60), null, null, null, null, null),
                listOf("Building again.", null, "Easy aerobic.", "8km with 5km at marathon pace.", null, "23km easy-steady.", "Rest.")))
            add(buildWeek(23, TrainingPhase.RACE_PREP, 65.0, "25K specific build",
                listOf(12.0, 0.0, 14.0, 13.0, 0.0, 26.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.TEMPO, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.RACE_25K, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Legs + Core", 60), null, null, null, null, null),
                listOf("25K race prep week.", null, "Easy.", "3×4km at 25K race pace.", null, "26km — practice race nutrition again.", "Rest.")))
            add(buildWeek(24, TrainingPhase.RACE_PREP, 55.0, "Cutback / pre-peak recovery",
                listOf(10.0, 0.0, 12.0, 10.0, 0.0, 20.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.RECOVERY_RUN, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.RECOVERY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Mobility", 45), null, null, null, null, null),
                listOf("Cutback. Let adaptation happen.", null, "Very easy.", "Easy effort.", null, "20km easy-steady.", "Rest.")))

            // PEAK TRAINING: Weeks 25–29 (Nov 16 – Dec 14) — pre 25K and then push to FM peak
            add(buildWeek(25, TrainingPhase.PEAK_TRAINING, 72.0, "Peak phase begins — marathon-specific work",
                listOf(14.0, 0.0, 15.0, 14.0, 0.0, 29.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength", 60), null, null, null, null, null),
                listOf("Peak phase! Embrace the work.", null, "Easy aerobic.", "14km with 8km at marathon pace.", null, "29km — practice race day fuelling.", "Recover fully.")))
            add(buildWeek(26, TrainingPhase.PEAK_TRAINING, 75.0, "High volume + marathon pace runs",
                listOf(14.0, 0.0, 15.0, 14.0, 0.0, 32.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Core + Strength", 60), null, null, null, null, null),
                listOf("Biggest week yet.", null, "Easy.", "15km with 10km at marathon pace.", null, "32km — a true long run!", "Full rest.")))
            add(buildWeek(27, TrainingPhase.PEAK_TRAINING, 70.0, "Cutback — absorb peak training",
                listOf(12.0, 0.0, 14.0, 12.0, 0.0, 26.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.RECOVERY_RUN, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.RECOVERY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Light Mobility", 45), null, null, null, null, null),
                listOf("Cutback. Let the 32km sink in.", null, "Very easy.", "Easy aerobic.", null, "26km easy.", "Rest.")))
            add(buildWeek(28, TrainingPhase.PEAK_TRAINING, 76.0, "Second peak week — final big push",
                listOf(14.0, 0.0, 15.0, 15.0, 0.0, 32.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Strength", 60), null, null, null, null, null),
                listOf("Second peak. This is your fitness peak.", null, "Easy.", "15km with 10km at marathon pace.", null, "32km — last 32km before taper.", "Full rest.")))
            add(buildWeekWithRaceDay(29, TrainingPhase.RACE_WEEK, 35.0, "25K RACE WEEK — Dec 20",
                LocalDate.of(2026, 12, 14),
                mapOf(
                    DayOfWeek.MONDAY to Triple(10.0, RunType.EASY, TrainingPaces.EASY),
                    DayOfWeek.TUESDAY to Triple(0.0, RunType.GYM_ONLY, null),
                    DayOfWeek.WEDNESDAY to Triple(8.0, RunType.EASY, TrainingPaces.EASY),
                    DayOfWeek.THURSDAY to Triple(6.0, RunType.STRIDES, TrainingPaces.STRIDES),
                    DayOfWeek.FRIDAY to Triple(0.0, RunType.REST, null),
                    DayOfWeek.SATURDAY to Triple(4.0, RunType.RECOVERY_RUN, TrainingPaces.RECOVERY),
                    DayOfWeek.SUNDAY to Triple(25.0, RunType.RACE, TrainingPaces.RACE_25K),
                ),
                mapOf(
                    DayOfWeek.MONDAY to "Easy — save legs for race day.",
                    DayOfWeek.TUESDAY to "Light gym only — activation.",
                    DayOfWeek.WEDNESDAY to "Short easy run to stay fresh.",
                    DayOfWeek.THURSDAY to "Strides — 6×100m to stay sharp.",
                    DayOfWeek.FRIDAY to "REST. Carb-load.",
                    DayOfWeek.SATURDAY to "4km shakeout — easy.",
                    DayOfWeek.SUNDAY to "RACE DAY — 25K! Target: sub-2:45.",
                ),
                "RACE",
                "25K Race"))

            // TAPER FOR FM: Weeks 30–32 (Dec 21 – Jan 10)
            add(buildWeek(30, TrainingPhase.TAPER, 60.0, "Post-25K recovery, start FM taper",
                listOf(10.0, 0.0, 12.0, 10.0, 0.0, 22.0, 0.0),
                listOf(RunType.RECOVERY_RUN, RunType.GYM_ONLY, RunType.EASY, RunType.EASY, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.RECOVERY, null, TrainingPaces.EASY, TrainingPaces.EASY, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Active Recovery", 30), null, null, null, null, null),
                listOf("Recovery from 25K. Easy week.", null, "Light aerobic.", "Easy effort.", null, "22km — reduced from peak.", "Rest.")))
            add(buildWeek(31, TrainingPhase.TAPER, 50.0, "FM taper deepens",
                listOf(10.0, 0.0, 10.0, 8.0, 0.0, 18.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.LONG, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.LONG_RUN, null),
                listOf(null, GymSession("Light Strength", 40), null, null, null, null, null),
                listOf("Taper continues. Legs should feel fresher.", null, "Easy.", "6km with 3km at marathon pace.", null, "18km easy — last moderate long run.", "Rest.")))
            add(buildWeek(32, TrainingPhase.TAPER, 38.0, "Final taper week — FM Jan 17",
                listOf(8.0, 0.0, 8.0, 6.0, 0.0, 8.0, 0.0),
                listOf(RunType.EASY, RunType.GYM_ONLY, RunType.EASY, RunType.MARATHON_PACE, RunType.REST, RunType.EASY, RunType.REST),
                listOf(TrainingPaces.EASY, null, TrainingPaces.EASY, TrainingPaces.MARATHON, null, TrainingPaces.EASY, null),
                listOf(null, GymSession("Light Activation", 25), null, null, null, null, null),
                listOf("Race week next week! Stay sharp.", null, "Easy.", "4km with 2km at marathon pace.", null, "Easy 8km. Stay off your feet.", "Full rest — race week starts Monday.")))

            // FM RACE WEEK: Week 33 (Jan 11–17)
            add(buildWeekWithRaceDay(33, TrainingPhase.RACE_WEEK, 58.0, "MARATHON RACE WEEK — Jan 17",
                LocalDate.of(2027, 1, 11),
                mapOf(
                    DayOfWeek.MONDAY to Triple(10.0, RunType.EASY, TrainingPaces.EASY),
                    DayOfWeek.TUESDAY to Triple(0.0, RunType.GYM_ONLY, null),
                    DayOfWeek.WEDNESDAY to Triple(8.0, RunType.EASY, TrainingPaces.EASY),
                    DayOfWeek.THURSDAY to Triple(6.0, RunType.STRIDES, TrainingPaces.STRIDES),
                    DayOfWeek.FRIDAY to Triple(0.0, RunType.REST, null),
                    DayOfWeek.SATURDAY to Triple(3.0, RunType.RECOVERY_RUN, TrainingPaces.RECOVERY),
                    DayOfWeek.SUNDAY to Triple(42.195, RunType.RACE, TrainingPaces.MARATHON),
                ),
                mapOf(
                    DayOfWeek.MONDAY to "Final week! Easy run to stay loose.",
                    DayOfWeek.TUESDAY to "Light gym — activation only. No heavy lifts.",
                    DayOfWeek.WEDNESDAY to "Easy 8km. Visualise race day.",
                    DayOfWeek.THURSDAY to "6×100m strides. Stay sharp.",
                    DayOfWeek.FRIDAY to "REST. Eat well. Sleep well. Lay out your race kit.",
                    DayOfWeek.SATURDAY to "3km easy shakeout. Nothing more.",
                    DayOfWeek.SUNDAY to "MARATHON DAY! Target: sub-4:30. You've earned this.",
                ),
                "RACE",
                "Full Marathon"))

            // RECOVERY: Weeks 34–35 (Jan 18 – Jan 31)
            add(buildWeek(34, TrainingPhase.RECOVERY, 20.0, "Post-marathon recovery week 1",
                listOf(0.0, 0.0, 5.0, 0.0, 0.0, 8.0, 0.0),
                listOf(RunType.REST, RunType.REST, RunType.RECOVERY_RUN, RunType.REST, RunType.REST, RunType.EASY, RunType.REST),
                listOf(null, null, TrainingPaces.RECOVERY, null, null, TrainingPaces.RECOVERY, null),
                listOf(null, null, null, null, null, null, null),
                listOf("You did it! Complete rest.", "Rest.", "First easy run post-marathon.", "Rest.", "Rest.", "Easy 8km — no pressure.", "Rest. Celebrate your achievement!")))
            add(buildWeek(35, TrainingPhase.RECOVERY, 25.0, "Post-marathon recovery week 2",
                listOf(0.0, 0.0, 6.0, 0.0, 5.0, 10.0, 0.0),
                listOf(RunType.REST, RunType.REST, RunType.RECOVERY_RUN, RunType.REST, RunType.EASY, RunType.EASY, RunType.REST),
                listOf(null, null, TrainingPaces.RECOVERY, null, TrainingPaces.EASY, TrainingPaces.EASY, null),
                listOf(null, null, null, null, null, null, null),
                listOf("Week 2 post-marathon.", "Rest.", "Easy recovery run.", "Rest.", "Easy aerobic.", "Easy long run — whatever feels good.", "Season complete!")))
        }
    }

    private fun buildWeek(
        weekNumber: Int,
        phase: TrainingPhase,
        totalKm: Double,
        keyWorkout: String,
        distances: List<Double>,
        runTypes: List<RunType>,
        paceRanges: List<PaceRange?>,
        gymSessions: List<GymSession?>,
        coachNotes: List<String>,
    ): WeekPlan {
        val startDate = PLAN_START_DATE.plusWeeks((weekNumber - 1).toLong())
        val days = DayOfWeek.entries.mapIndexed { index, dow ->
            val date = startDate.plusDays(index.toLong())
            DayWorkout(
                date = date,
                weekNumber = weekNumber,
                dayOfWeek = dow,
                phase = phase,
                runType = runTypes[index],
                distanceKm = distances[index],
                paceRange = paceRanges[index],
                gymSession = gymSessions[index],
                coachNote = coachNotes[index],
                isRaceDay = false,
                raceName = null,
            )
        }
        return WeekPlan(weekNumber, phase, startDate, days, totalKm, keyWorkout)
    }

    private fun buildWeekWithRaceDay(
        weekNumber: Int,
        phase: TrainingPhase,
        totalKm: Double,
        keyWorkout: String,
        weekStartDate: LocalDate,
        dayMap: Map<DayOfWeek, Triple<Double, RunType, PaceRange?>>,
        noteMap: Map<DayOfWeek, String>,
        raceLabel: String,
        raceName: String,
    ): WeekPlan {
        val days = DayOfWeek.entries.mapIndexed { index, dow ->
            val date = weekStartDate.plusDays(index.toLong())
            val (dist, runType, pace) = dayMap[dow] ?: Triple(0.0, RunType.REST, null)
            val isRace = runType == RunType.RACE
            val gym = if (runType == RunType.GYM_ONLY) GymSession("Activation", 20) else null
            DayWorkout(
                date = date,
                weekNumber = weekNumber,
                dayOfWeek = dow,
                phase = phase,
                runType = runType,
                distanceKm = dist,
                paceRange = pace,
                gymSession = gym,
                coachNote = noteMap[dow],
                isRaceDay = isRace,
                raceName = if (isRace) raceName else null,
            )
        }
        return WeekPlan(weekNumber, phase, weekStartDate, days, totalKm, keyWorkout)
    }

    fun getWorkoutForDate(date: LocalDate): DayWorkout? {
        if (date < PLAN_START_DATE || date > PLAN_END_DATE) return null
        val weekIndex = ChronoUnit.WEEKS.between(PLAN_START_DATE, date).toInt()
        val week = ALL_WEEKS.getOrNull(weekIndex) ?: return null
        return week.days.find { it.date == date }
    }

    fun getWeekNumberForDate(date: LocalDate): Int {
        if (date < PLAN_START_DATE) return 0
        if (date > PLAN_END_DATE) return 35
        return (ChronoUnit.WEEKS.between(PLAN_START_DATE, date) + 1).toInt().coerceIn(1, 35)
    }

    fun getWeekPlan(weekNumber: Int): WeekPlan? =
        ALL_WEEKS.getOrNull(weekNumber - 1)

    fun getNextRace(fromDate: LocalDate): Race =
        RACES.firstOrNull { it.date >= fromDate } ?: RACES.last()
}
