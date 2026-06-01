package com.marathon.tracker.domain.usecase

import com.marathon.tracker.auth.StravaAuthManager
import com.marathon.tracker.auth.TokenManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshTokenIfNeededUseCaseTest {

    private val tokenManager: TokenManager = mockk()
    private val stravaAuthManager: StravaAuthManager = mockk()
    private lateinit var useCase: RefreshTokenIfNeededUseCase

    @Before
    fun setUp() {
        useCase = RefreshTokenIfNeededUseCase(tokenManager, stravaAuthManager)
    }

    @Test
    fun `returns success without refreshing when no valid tokens`() = runTest {
        every { tokenManager.hasValidTokens() } returns false

        val result = useCase()

        assertTrue(result.isSuccess)
        verify(exactly = 0) { tokenManager.isTokenExpiringSoon() }
        coVerify(exactly = 0) { stravaAuthManager.refreshTokenIfNeeded() }
    }

    @Test
    fun `returns success without refreshing when token is not expiring soon`() = runTest {
        every { tokenManager.hasValidTokens() } returns true
        every { tokenManager.isTokenExpiringSoon() } returns false

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { stravaAuthManager.refreshTokenIfNeeded() }
    }

    @Test
    fun `refreshes token when expiring within 600 seconds`() = runTest {
        every { tokenManager.hasValidTokens() } returns true
        every { tokenManager.isTokenExpiringSoon() } returns true
        coEvery { stravaAuthManager.refreshTokenIfNeeded() } returns Result.success(Unit)

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { stravaAuthManager.refreshTokenIfNeeded() }
    }

    @Test
    fun `propagates refresh failure`() = runTest {
        every { tokenManager.hasValidTokens() } returns true
        every { tokenManager.isTokenExpiringSoon() } returns true
        coEvery { stravaAuthManager.refreshTokenIfNeeded() } returns Result.failure(RuntimeException("Network error"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network") == true)
    }
}
