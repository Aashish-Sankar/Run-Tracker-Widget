package com.marathon.tracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CoachingRepositoryTest {

    private val claudeApi: com.marathon.tracker.data.remote.api.ClaudeApi = mockk()
    private val dataStore: DataStore<Preferences> = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private lateinit var repository: CoachingRepositoryImpl

    @Before
    fun setUp() {
        val emptyPrefs: Preferences = mutablePreferencesOf()
        every { dataStore.data } returns flowOf(emptyPrefs)
        repository = CoachingRepositoryImpl(claudeApi, dataStore, userPreferencesRepository)
    }

    @Test
    fun `canGenerateReport returns true when no previous report`() = runTest {
        every { userPreferencesRepository.getLastReportDateEpochDay() } returns flowOf(null)

        val canGenerate = repository.canGenerateReport().first()

        assertTrue(canGenerate)
    }

    @Test
    fun `canGenerateReport returns false when report was generated today`() = runTest {
        val todayEpochDay = LocalDate.now().toEpochDay()
        every { userPreferencesRepository.getLastReportDateEpochDay() } returns flowOf(todayEpochDay)

        val canGenerate = repository.canGenerateReport().first()

        assertFalse(canGenerate)
    }

    @Test
    fun `canGenerateReport returns true when report was generated yesterday`() = runTest {
        val yesterdayEpochDay = LocalDate.now().minusDays(1).toEpochDay()
        every { userPreferencesRepository.getLastReportDateEpochDay() } returns flowOf(yesterdayEpochDay)

        val canGenerate = repository.canGenerateReport().first()

        assertTrue(canGenerate)
    }

    @Test
    fun `getLatestReport returns null when no cached report`() = runTest {
        val emptyPrefs: Preferences = mutablePreferencesOf()
        every { dataStore.data } returns flowOf(emptyPrefs)

        val report = repository.getLatestReport().first()

        assertTrue(report == null)
    }

    @Test
    fun `getLatestReport returns null when json is malformed`() = runTest {
        val prefs = mutablePreferencesOf(stringPreferencesKey("coaching_report_json") to "{invalid json}")
        every { dataStore.data } returns flowOf(prefs)

        val report = repository.getLatestReport().first()

        assertTrue(report == null)
    }
}
