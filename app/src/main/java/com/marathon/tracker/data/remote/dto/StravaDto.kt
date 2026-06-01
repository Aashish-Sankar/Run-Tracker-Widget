package com.marathon.tracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StravaTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_at") val expiresAt: Long,
    @SerializedName("athlete") val athlete: StravaAthleteDto,
)

data class StravaAthleteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("profile") val profileUrl: String,
)

data class StravaActivityDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("start_date_local") val startDateLocal: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("moving_time") val movingTime: Int,
    @SerializedName("average_speed") val averageSpeed: Double,
    @SerializedName("average_heartrate") val averageHeartrate: Double?,
    @SerializedName("max_heartrate") val maxHeartrate: Double?,
    @SerializedName("total_elevation_gain") val totalElevationGain: Double,
    @SerializedName("kudos_count") val kudosCount: Int,
    @SerializedName("map") val map: StravaMapDto?,
)

data class StravaMapDto(
    @SerializedName("summary_polyline") val summaryPolyline: String?,
)

data class StravaExchangeRequest(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("code") val code: String,
    @SerializedName("grant_type") val grantType: String = "authorization_code",
)

data class StravaRefreshRequest(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("grant_type") val grantType: String = "refresh_token",
)
