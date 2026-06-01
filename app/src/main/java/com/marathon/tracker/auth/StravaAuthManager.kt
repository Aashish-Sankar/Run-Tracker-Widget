package com.marathon.tracker.auth

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.marathon.tracker.BuildConfig
import com.marathon.tracker.data.remote.api.StravaAuthApi
import com.marathon.tracker.data.remote.dto.StravaExchangeRequest
import com.marathon.tracker.data.remote.dto.StravaRefreshRequest
import com.marathon.tracker.domain.model.AthleteInfo
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StravaAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stravaAuthApi: StravaAuthApi,
    private val tokenManager: TokenManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun buildAuthUri(): Uri =
        Uri.parse("https://www.strava.com/oauth/authorize").buildUpon()
            .appendQueryParameter("client_id", BuildConfig.STRAVA_CLIENT_ID)
            .appendQueryParameter("redirect_uri", "marathon://marathon/callback")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read_all,profile:read_all")
            .build()

    fun launchAuthFlow(activity: ComponentActivity) {
        val uri = buildAuthUri()
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(activity, uri)
    }

    suspend fun handleCallback(code: String): Result<AthleteInfo> = runCatching {
        val response = stravaAuthApi.exchangeCode(
            StravaExchangeRequest(
                clientId = BuildConfig.STRAVA_CLIENT_ID,
                clientSecret = BuildConfig.STRAVA_CLIENT_SECRET,
                code = code,
            )
        )
        tokenManager.saveTokens(response.accessToken, response.refreshToken, response.expiresAt)
        val info = AthleteInfo(
            id = response.athlete.id,
            firstName = response.athlete.firstName,
            lastName = response.athlete.lastName,
            profileUrl = response.athlete.profileUrl,
        )
        userPreferencesRepository.saveAthleteInfo(info)
        info
    }

    suspend fun refreshTokenIfNeeded(): Result<Unit> = runCatching {
        if (!tokenManager.isTokenExpiringSoon()) return@runCatching
        val refreshToken = tokenManager.getRefreshToken()
            ?: error("No refresh token available")
        val response = stravaAuthApi.refreshToken(
            StravaRefreshRequest(
                clientId = BuildConfig.STRAVA_CLIENT_ID,
                clientSecret = BuildConfig.STRAVA_CLIENT_SECRET,
                refreshToken = refreshToken,
            )
        )
        tokenManager.saveTokens(response.accessToken, response.refreshToken, response.expiresAt)
    }

    fun disconnect() {
        tokenManager.clearTokens()
    }

    fun isConnected(): Boolean = tokenManager.hasValidTokens()
}
