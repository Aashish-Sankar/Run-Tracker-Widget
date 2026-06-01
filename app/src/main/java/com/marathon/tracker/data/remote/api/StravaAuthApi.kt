package com.marathon.tracker.data.remote.api

import com.marathon.tracker.data.remote.dto.StravaExchangeRequest
import com.marathon.tracker.data.remote.dto.StravaRefreshRequest
import com.marathon.tracker.data.remote.dto.StravaTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface StravaAuthApi {
    @POST("token")
    suspend fun exchangeCode(@Body request: StravaExchangeRequest): StravaTokenResponse

    @POST("token")
    suspend fun refreshToken(@Body request: StravaRefreshRequest): StravaTokenResponse
}
