package com.marathon.tracker.domain.usecase

import com.marathon.tracker.auth.TokenManager
import com.marathon.tracker.auth.StravaAuthManager
import javax.inject.Inject

class RefreshTokenIfNeededUseCase @Inject constructor(
    private val tokenManager: TokenManager,
    private val stravaAuthManager: StravaAuthManager,
) {
    suspend operator fun invoke(): Result<Unit> {
        if (!tokenManager.hasValidTokens()) return Result.success(Unit)
        if (!tokenManager.isTokenExpiringSoon()) return Result.success(Unit)
        return stravaAuthManager.refreshTokenIfNeeded()
    }
}
