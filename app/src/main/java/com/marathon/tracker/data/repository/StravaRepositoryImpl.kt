package com.marathon.tracker.data.repository

import com.marathon.tracker.auth.TokenManager
import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.entity.StravaActivityEntity
import com.marathon.tracker.data.remote.api.StravaApi
import com.marathon.tracker.domain.model.AthleteInfo
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.repository.StravaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StravaRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaActivityDao: StravaActivityDao,
    private val tokenManager: TokenManager,
) : StravaRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    override suspend fun syncActivities(): Result<Int> = runCatching {
        val latestMs = stravaActivityDao.getLatestActivityTimestampMillis()
        val afterEpochSeconds = if (latestMs != null) latestMs / 1000 else null

        val dtos = stravaApi.getActivities(after = afterEpochSeconds, perPage = 30)
        val entities = dtos.map { dto ->
            val startMillis = parseIsoDate(dto.startDateLocal)
            val epochDay = LocalDate.ofEpochDay(startMillis / 86_400_000).toEpochDay()
            StravaActivityEntity(
                id = dto.id,
                name = dto.name,
                type = dto.type,
                startDateEpochDay = epochDay,
                startDateMillis = startMillis,
                distanceMeters = dto.distance,
                movingTimeSeconds = dto.movingTime,
                averageSpeedMps = dto.averageSpeed,
                averageHeartrate = dto.averageHeartrate,
                maxHeartrate = dto.maxHeartrate,
                totalElevationGain = dto.totalElevationGain,
                kudosCount = dto.kudosCount,
                mapPolyline = dto.map?.summaryPolyline,
                syncedAtMillis = System.currentTimeMillis(),
            )
        }
        stravaActivityDao.upsertActivities(entities)
        entities.size
    }

    override fun getRecentActivities(limit: Int): Flow<List<StravaActivity>> =
        stravaActivityDao.getRecentActivities(limit).map { list -> list.map { it.toDomain() } }

    override fun getLastActivity(): Flow<StravaActivity?> =
        stravaActivityDao.getLastActivity().map { it?.toDomain() }

    override fun isConnected(): Boolean = tokenManager.hasValidTokens()

    override suspend fun getAthleteInfo(): Result<AthleteInfo> = runCatching {
        val dto = stravaApi.getAthlete()
        AthleteInfo(
            id = dto.id,
            firstName = dto.firstName,
            lastName = dto.lastName,
            profileUrl = dto.profileUrl,
        )
    }

    private fun parseIsoDate(dateStr: String): Long {
        return try {
            val ldt = LocalDateTime.parse(dateStr, dateFormatter)
            ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun StravaActivityEntity.toDomain(): StravaActivity {
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
