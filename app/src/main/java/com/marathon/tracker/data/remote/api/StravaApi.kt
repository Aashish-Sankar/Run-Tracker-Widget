package com.marathon.tracker.data.remote.api

import com.marathon.tracker.data.remote.dto.StravaActivityDto
import com.marathon.tracker.data.remote.dto.StravaAthleteDto
import retrofit2.http.GET
import retrofit2.http.Query

interface StravaApi {
    @GET("athlete/activities")
    suspend fun getActivities(
        @Query("after") after: Long? = null,
        @Query("before") before: Long? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
    ): List<StravaActivityDto>

    @GET("athlete")
    suspend fun getAthlete(): StravaAthleteDto
}
