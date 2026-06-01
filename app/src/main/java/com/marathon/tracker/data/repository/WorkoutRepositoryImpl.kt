package com.marathon.tracker.data.repository

import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import com.marathon.tracker.data.local.entity.WorkoutLogEntity
import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.model.WeekSummary
import com.marathon.tracker.domain.model.WorkoutLog
import com.marathon.tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutLogDao: WorkoutLogDao,
    private val stravaActivityDao: StravaActivityDao,
) : WorkoutRepository {

    override fun getTodayWorkout(): Flow<TodayWorkout> {
        val today = LocalDate.now()
        val epochDay = today.toEpochDay()
        val plan = WorkoutData.getWorkoutForDate(today)

        return combine(
            flow { emit(workoutLogDao.getLogForDate(epochDay)) },
            stravaActivityDao.getRunForDate(epochDay).let { flow { emit(it) } },
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

    override fun getWeekSummary(weekNumber: Int): Flow<WeekSummary> {
        val weekPlan = WorkoutData.getWeekPlan(weekNumber)
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
                phase = weekPlan?.phase ?: com.marathon.tracker.domain.model.TrainingPhase.BASE_BUILDING,
                startDate = weekPlan?.startDate ?: WorkoutData.PLAN_START_DATE,
            )
        }
    }

    override suspend fun markWorkoutCompleted(
        date: LocalDate,
        actualKm: Double,
        actualPaceSecPerKm: Double,
    ) {
        val epochDay = date.toEpochDay()
        val weekNumber = WorkoutData.getWeekNumberForDate(date)
        val plan = WorkoutData.getWorkoutForDate(date)
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
        WorkoutData.getWorkoutForDate(date)

    override fun getAllWeeks(): List<WeekPlan> = WorkoutData.ALL_WEEKS

    override fun getWeekPlan(weekNumber: Int): WeekPlan? =
        WorkoutData.getWeekPlan(weekNumber)

    override fun getCurrentWeekNumber(): Int =
        WorkoutData.getWeekNumberForDate(LocalDate.now())

    fun matchStravaActivityToPlanDay(activity: StravaActivity, plan: DayWorkout): Boolean {
        if (plan.runType == RunType.REST || plan.runType == RunType.GYM_ONLY) return false
        if (activity.type != "Run") return false
        if (plan.distanceKm == 0.0) return false
        val distanceDiff = kotlin.math.abs(activity.distanceKm - plan.distanceKm)
        return distanceDiff <= 3.0
    }

    private fun WorkoutLog.toDomain() = this

    private suspend fun StravaActivityDao.getRunForDate(epochDay: Long) =
        getRunForDate(epochDay)

    private fun com.marathon.tracker.data.local.entity.WorkoutLogEntity.toDomain(): WorkoutLog =
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
