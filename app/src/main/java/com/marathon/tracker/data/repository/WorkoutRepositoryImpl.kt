package com.marathon.tracker.data.repository

import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import com.marathon.tracker.data.local.entity.WorkoutLogEntity
import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.di.ApplicationScope
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.model.WeekSummary
import com.marathon.tracker.domain.model.WorkoutLog
import com.marathon.tracker.domain.repository.PlanRepository
import com.marathon.tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutLogDao: WorkoutLogDao,
    private val stravaActivityDao: StravaActivityDao,
    private val planRepository: PlanRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : WorkoutRepository {

    private val cachedWeeks = MutableStateFlow<List<WeekPlan>>(WorkoutData.ALL_WEEKS)
    private val cachedRaces = MutableStateFlow<List<Race>>(WorkoutData.RACES)

    init {
        applicationScope.launch {
            planRepository.observeActivePlan().collect { plan ->
                cachedWeeks.value = plan?.weeks?.ifEmpty { null } ?: WorkoutData.ALL_WEEKS
                cachedRaces.value = plan?.races?.ifEmpty { null } ?: WorkoutData.RACES
            }
        }
    }

    override fun getTodayWorkout(): Flow<TodayWorkout> {
        val today = LocalDate.now()
        val epochDay = today.toEpochDay()

        return cachedWeeks.flatMapLatest { weeks ->
            val plan = weeks.flatMap { it.days }.find { it.date == today }

            combine(
                flow { emit(workoutLogDao.getLogForDate(epochDay)) },
                flow { emit(stravaActivityDao.getRunForDate(epochDay)) },
            ) { logEntity, stravaEntity ->
                val log = logEntity?.toDomain()
                val stravaActivity = stravaEntity?.toDomain()
                val matched = if (plan != null && stravaActivity != null) {
                    matchStravaActivityToPlanDay(stravaActivity, plan)
                } else false

                TodayWorkout(
                    plan = plan,
                    logEntry = log,
                    matchedStravaActivity = if (matched) stravaActivity else null,
                    isCompleted = log?.isCompleted == true || matched,
                )
            }
        }
    }

    override fun getWeekSummary(weekNumber: Int): Flow<WeekSummary> {
        val weekPlan = getWeekPlan(weekNumber)
        return combine(
            workoutLogDao.getWeeklyActualKm(weekNumber),
            workoutLogDao.getCompletedDaysInWeek(weekNumber),
        ) { actualKm, completedDays ->
            WeekSummary(
                weekNumber = weekNumber,
                plannedKm = weekPlan?.totalPlannedKm ?: 0.0,
                actualKm = actualKm,
                completedDays = completedDays,
                totalDays = 7,
                phase = weekPlan?.phase ?: TrainingPhase.BASE_BUILDING,
                startDate = weekPlan?.startDate ?: cachedWeeks.value.firstOrNull()?.startDate
                    ?: WorkoutData.PLAN_START_DATE,
            )
        }
    }

    override suspend fun markWorkoutCompleted(
        date: LocalDate,
        actualKm: Double,
        actualPaceSecPerKm: Double,
    ) {
        val epochDay = date.toEpochDay()
        val plan = getWorkoutForDate(date)
        val weekNumber = cachedWeeks.value
            .find { wp -> wp.days.any { it.date == date } }?.weekNumber ?: 1
        val existing = workoutLogDao.getLogForDate(epochDay)
        val id = existing?.id ?: UUID.randomUUID().toString()

        workoutLogDao.upsertLog(
            WorkoutLogEntity(
                id = id,
                dateEpochDay = epochDay,
                weekNumber = weekNumber,
                plannedDistanceKm = plan?.distanceKm ?: 0.0,
                plannedRunType = plan?.runType?.name ?: RunType.EASY.name,
                actualDistanceKm = actualKm,
                actualPaceSecPerKm = actualPaceSecPerKm,
                stravaActivityId = existing?.stravaActivityId,
                isCompleted = true,
                notes = null,
                completedAtMillis = System.currentTimeMillis(),
            )
        )
    }

    override fun getWorkoutForDate(date: LocalDate): DayWorkout? =
        cachedWeeks.value.flatMap { it.days }.find { it.date == date }

    override fun getAllWeeks(): List<WeekPlan> = cachedWeeks.value

    override fun getWeekPlan(weekNumber: Int): WeekPlan? =
        cachedWeeks.value.find { it.weekNumber == weekNumber }

    override fun getCurrentWeekNumber(): Int =
        cachedWeeks.value
            .find { wp -> wp.days.any { it.date == LocalDate.now() } }?.weekNumber
            ?: cachedWeeks.value.firstOrNull()?.weekNumber
            ?: 1

    fun matchStravaActivityToPlanDay(activity: StravaActivity, plan: DayWorkout): Boolean {
        if (plan.runType == RunType.REST || plan.runType == RunType.GYM_ONLY) return false
        if (activity.type != "Run") return false
        if (plan.distanceKm == 0.0) return false
        return kotlin.math.abs(activity.distanceKm - plan.distanceKm) <= 3.0
    }

    private fun WorkoutLogEntity.toDomain(): WorkoutLog =
        WorkoutLog(
            id = id,
            date = LocalDate.ofEpochDay(dateEpochDay),
            actualDistanceKm = actualDistanceKm,
            actualPaceSecPerKm = actualPaceSecPerKm,
            stravaActivityId = stravaActivityId,
            isCompleted = isCompleted,
            notes = notes,
        )

    private fun com.marathon.tracker.data.local.entity.StravaActivityEntity.toDomain(): StravaActivity {
        val distanceKm = distanceMeters / 1000.0
        val paceSecPerKm = if (averageSpeedMps > 0) 1000.0 / averageSpeedMps else 0.0
        return StravaActivity(
            id = id,
            name = name,
            type = type,
            startDate = LocalDate.ofEpochDay(startDateEpochDay),
            distanceKm = distanceKm,
            movingTimeSeconds = movingTimeSeconds,
            averagePaceSecPerKm = paceSecPerKm,
            averageHeartrate = averageHeartrate,
            maxHeartrate = maxHeartrate,
            totalElevationGain = totalElevationGain,
            kudosCount = kudosCount,
            mapPolyline = mapPolyline,
        )
    }
}
