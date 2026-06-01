package com.marathon.tracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.marathon.tracker.data.local.dao.TrainingPlanDao
import com.marathon.tracker.data.local.dto.DayWorkoutDto
import com.marathon.tracker.data.local.dto.RaceDto
import com.marathon.tracker.data.local.dto.TrainingPlanDto
import com.marathon.tracker.data.local.dto.WeekPlanDto
import com.marathon.tracker.data.local.entity.TrainingPlanEntity
import com.marathon.tracker.data.workout.PlanGenerator
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.GymSession
import com.marathon.tracker.domain.model.PaceRange
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.TrainingPlan
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val trainingPlanDao: TrainingPlanDao,
    private val dataStore: DataStore<Preferences>,
) : PlanRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val defaultPlanSeededKey = booleanPreferencesKey("default_plan_seeded")

    override fun getAllPlans(): Flow<List<TrainingPlan>> =
        trainingPlanDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeActivePlan(): Flow<TrainingPlan?> =
        trainingPlanDao.observeActive().map { entity ->
            entity?.toDomain()
        }

    override suspend fun getActivePlan(): TrainingPlan? =
        trainingPlanDao.getActive()?.toDomain()

    override suspend fun activatePlan(planId: String) {
        trainingPlanDao.deactivateAll()
        val current = trainingPlanDao.getAll().first().find { it.id == planId } ?: return
        trainingPlanDao.upsert(current.copy(isActive = true))
    }

    override suspend fun importPlan(planDto: TrainingPlanDto): TrainingPlan {
        val id = UUID.randomUUID().toString()
        val startDate = LocalDate.parse(planDto.startDate)
        val weeks = planDto.weeks.map { it.toWeekPlan() }
        val races = planDto.races.map { it.toRace() }

        val entity = TrainingPlanEntity(
            id = id,
            name = planDto.name,
            startDateEpochDay = startDate.toEpochDay(),
            targetMarathonSeconds = planDto.targetMarathonSeconds,
            racesJson = json.encodeToString(planDto.races),
            weeksJson = json.encodeToString(planDto.weeks),
            isActive = false,
            isDefault = false,
            createdAtMillis = System.currentTimeMillis(),
        )
        trainingPlanDao.upsert(entity)

        return TrainingPlan(
            id = id,
            name = planDto.name,
            startDate = startDate,
            targetMarathonSeconds = planDto.targetMarathonSeconds,
            races = races,
            weeks = weeks,
            isDefault = false,
            createdAtMillis = entity.createdAtMillis,
        )
    }

    override suspend fun deletePlan(planId: String) {
        trainingPlanDao.deleteById(planId)
    }

    override suspend fun createPlan(
        name: String,
        startDate: LocalDate,
        targetMarathonSeconds: Int?,
        races: List<Race>,
    ): TrainingPlan {
        val id = UUID.randomUUID().toString()
        val weeks = PlanGenerator.generate(startDate, targetMarathonSeconds, races)

        val weekDtos = weeks.map { it.toDto() }
        val raceDtos = races.map { it.toDto() }

        val entity = TrainingPlanEntity(
            id = id,
            name = name,
            startDateEpochDay = startDate.toEpochDay(),
            targetMarathonSeconds = targetMarathonSeconds,
            racesJson = json.encodeToString(raceDtos),
            weeksJson = json.encodeToString(weekDtos),
            isActive = false,
            isDefault = false,
            createdAtMillis = System.currentTimeMillis(),
        )
        trainingPlanDao.upsert(entity)

        return TrainingPlan(
            id = id,
            name = name,
            startDate = startDate,
            targetMarathonSeconds = targetMarathonSeconds,
            races = races,
            weeks = weeks,
            isDefault = false,
            createdAtMillis = entity.createdAtMillis,
        )
    }

    override suspend fun seedDefaultIfNeeded(defaultWeeks: List<WeekPlan>, defaultRaces: List<Race>) {
        val prefs = dataStore.data.first()
        val alreadySeeded = prefs[defaultPlanSeededKey] ?: false
        if (alreadySeeded) return

        val weekDtos = defaultWeeks.map { it.toDto() }
        val raceDtos = defaultRaces.map { it.toDto() }
        val startDate = defaultWeeks.firstOrNull()?.startDate ?: LocalDate.now()

        val entity = TrainingPlanEntity(
            id = "default",
            name = "MilePost Default Plan",
            startDateEpochDay = startDate.toEpochDay(),
            targetMarathonSeconds = null,
            racesJson = json.encodeToString(raceDtos),
            weeksJson = json.encodeToString(weekDtos),
            isActive = true,
            isDefault = true,
            createdAtMillis = System.currentTimeMillis(),
        )
        trainingPlanDao.deactivateAll()
        trainingPlanDao.upsert(entity)

        dataStore.edit { it[defaultPlanSeededKey] = true }
    }

    // ---- Mapping helpers ----

    private fun TrainingPlanEntity.toDomain(): TrainingPlan {
        val raceDtos = json.decodeFromString<List<RaceDto>>(racesJson)
        val weekDtos = json.decodeFromString<List<WeekPlanDto>>(weeksJson)
        return TrainingPlan(
            id = id,
            name = name,
            startDate = LocalDate.ofEpochDay(startDateEpochDay),
            targetMarathonSeconds = targetMarathonSeconds,
            races = raceDtos.map { it.toRace() },
            weeks = weekDtos.map { it.toWeekPlan() },
            isDefault = isDefault,
            createdAtMillis = createdAtMillis,
        )
    }

    private fun RaceDto.toRace() = Race(
        name = name,
        date = LocalDate.parse(date),
        distanceKm = distanceKm,
        targetFinishSeconds = targetFinishSeconds,
    )

    private fun Race.toDto() = RaceDto(
        name = name,
        date = date.toString(),
        distanceKm = distanceKm,
        targetFinishSeconds = targetFinishSeconds,
    )

    private fun WeekPlanDto.toWeekPlan(): WeekPlan {
        val resolvedPhase = TrainingPhase.valueOf(phase)
        val days = days.map { it.toDayWorkout(weekNumber, resolvedPhase) }
        val startDate = days.firstOrNull()?.date ?: LocalDate.now()
        return WeekPlan(
            weekNumber = weekNumber,
            phase = resolvedPhase,
            startDate = startDate,
            days = days,
            totalPlannedKm = days.sumOf { it.distanceKm },
            keyWorkoutDescription = keyWorkoutDescription,
        )
    }

    private fun WeekPlan.toDto(): WeekPlanDto = WeekPlanDto(
        weekNumber = weekNumber,
        phase = phase.name,
        keyWorkoutDescription = keyWorkoutDescription,
        days = days.map { it.toDto() },
    )

    private fun DayWorkoutDto.toDayWorkout(weekNumber: Int, phase: TrainingPhase): DayWorkout {
        val paceRange = if (paceMinSecondsPerKm != null && paceMaxSecondsPerKm != null) {
            PaceRange(paceMinSecondsPerKm, paceMaxSecondsPerKm)
        } else null

        val gymSession = if (gymSessionFocus != null) {
            GymSession(
                focus = gymSessionFocus,
                durationMinutes = gymSessionDurationMinutes ?: 45,
            )
        } else null

        return DayWorkout(
            date = LocalDate.parse(date),
            weekNumber = weekNumber,
            dayOfWeek = DayOfWeek.valueOf(dayOfWeek),
            phase = phase,
            runType = RunType.valueOf(runType),
            distanceKm = distanceKm,
            paceRange = paceRange,
            gymSession = gymSession,
            coachNote = coachNote,
            isRaceDay = isRaceDay,
            raceName = raceName,
        )
    }

    private fun DayWorkout.toDto() = DayWorkoutDto(
        date = date.toString(),
        dayOfWeek = dayOfWeek.name,
        runType = runType.name,
        distanceKm = distanceKm,
        paceMinSecondsPerKm = paceRange?.minSecondsPerKm,
        paceMaxSecondsPerKm = paceRange?.maxSecondsPerKm,
        gymSessionFocus = gymSession?.focus,
        gymSessionDurationMinutes = gymSession?.durationMinutes,
        coachNote = coachNote,
        isRaceDay = isRaceDay,
        raceName = raceName,
    )
}
