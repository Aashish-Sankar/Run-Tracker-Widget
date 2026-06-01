package com.marathon.tracker.di

import com.marathon.tracker.auth.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.marathon.tracker.BuildConfig
import com.marathon.tracker.data.remote.api.ClaudeApi
import com.marathon.tracker.data.remote.api.StravaApi
import com.marathon.tracker.data.remote.api.StravaAuthApi
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    @Named("strava_auth")
    fun provideStravaAuthInterceptor(): Interceptor = Interceptor { chain ->
        chain.proceed(chain.request())
    }

    @Provides
    @Singleton
    @Named("strava_api")
    fun provideStravaApiInterceptor(tokenManager: TokenManager): Interceptor = Interceptor { chain ->
        val token = tokenManager.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    @Provides
    @Singleton
    @Named("claude")
    fun provideClaudeInterceptor(tokenManager: TokenManager): Interceptor = Interceptor { chain ->
        val apiKey = tokenManager.getClaudeApiKey()
            ?: BuildConfig.CLAUDE_API_KEY.takeIf { it.isNotEmpty() }
            ?: ""
        val request = chain.request().newBuilder()
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    @Named("base")
    fun provideBaseOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("strava_api")
    fun provideStravaApiOkHttpClient(
        @Named("base") baseClient: OkHttpClient,
        @Named("strava_api") authInterceptor: Interceptor,
    ): OkHttpClient = baseClient.newBuilder()
        .addInterceptor(authInterceptor)
        .build()

    @Provides
    @Singleton
    @Named("strava_auth")
    fun provideStravaAuthOkHttpClient(
        @Named("base") baseClient: OkHttpClient,
    ): OkHttpClient = baseClient

    @Provides
    @Singleton
    @Named("claude")
    fun provideClaudeOkHttpClient(
        @Named("base") baseClient: OkHttpClient,
        @Named("claude") claudeInterceptor: Interceptor,
    ): OkHttpClient = baseClient.newBuilder()
        .addInterceptor(claudeInterceptor)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideStravaApi(@Named("strava_api") client: OkHttpClient): StravaApi =
        Retrofit.Builder()
            .baseUrl("https://www.strava.com/api/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StravaApi::class.java)

    @Provides
    @Singleton
    fun provideStravaAuthApi(@Named("strava_auth") client: OkHttpClient): StravaAuthApi =
        Retrofit.Builder()
            .baseUrl("https://www.strava.com/oauth/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StravaAuthApi::class.java)

    @Provides
    @Singleton
    fun provideClaudeApi(@Named("claude") client: OkHttpClient): ClaudeApi =
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClaudeApi::class.java)
}
