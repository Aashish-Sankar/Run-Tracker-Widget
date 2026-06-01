package com.marathon.tracker.auth

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val encryptedPrefs: SharedPreferences,
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "strava_access_token"
        private const val KEY_REFRESH_TOKEN = "strava_refresh_token"
        private const val KEY_EXPIRES_AT = "strava_expires_at"
        private const val KEY_CLAUDE_API_KEY = "claude_api_key"
        private const val EXPIRY_BUFFER_SECONDS = 600L
    }

    fun getAccessToken(): String? = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    fun getExpiresAt(): Long = encryptedPrefs.getLong(KEY_EXPIRES_AT, 0L)

    fun hasValidTokens(): Boolean {
        val token = getAccessToken() ?: return false
        return token.isNotEmpty() && getExpiresAt() > System.currentTimeMillis() / 1000
    }

    fun isTokenExpiringSoon(): Boolean {
        val expiresAt = getExpiresAt()
        if (expiresAt == 0L) return false
        return expiresAt - System.currentTimeMillis() / 1000 < EXPIRY_BUFFER_SECONDS
    }

    fun saveTokens(accessToken: String, refreshToken: String, expiresAt: Long) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    fun getClaudeApiKey(): String? = encryptedPrefs.getString(KEY_CLAUDE_API_KEY, null)

    fun saveClaudeApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_CLAUDE_API_KEY, key).apply()
    }
}
