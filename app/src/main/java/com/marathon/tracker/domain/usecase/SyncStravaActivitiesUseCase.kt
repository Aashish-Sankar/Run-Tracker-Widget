package com.marathon.tracker.domain.usecase

import com.marathon.tracker.domain.repository.StravaRepository
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SyncStravaActivitiesUseCase @Inject constructor(
    private val stravaRepository: StravaRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(): Result<Int> {
        val result = stravaRepository.syncActivities()
        if (result.isSuccess) {
            userPreferencesRepository.setLastSyncTimeMillis(System.currentTimeMillis())
        }
        return result
    }
}
