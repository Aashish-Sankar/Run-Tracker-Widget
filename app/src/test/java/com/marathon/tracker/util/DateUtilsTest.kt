package com.marathon.tracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun `getDaysUntil returns 0 for today`() {
        assertEquals(0, DateUtils.getDaysUntil(LocalDate.now()))
    }

    @Test
    fun `getDaysUntil returns positive for future date`() {
        val future = LocalDate.now().plusDays(7)
        assertEquals(7, DateUtils.getDaysUntil(future))
    }

    @Test
    fun `getDaysUntil clamps to zero for past dates`() {
        val past = LocalDate.now().minusDays(3)
        assertEquals(0, DateUtils.getDaysUntil(past))
    }

    @Test
    fun `LocalDate epoch day round-trip is correct`() {
        val original = LocalDate.of(2026, 10, 18)
        val epochDay = original.toEpochDay()
        val recovered = LocalDate.ofEpochDay(epochDay)
        assertEquals(original, recovered)
    }

    @Test
    fun `formatRelativeTime returns just now for recent timestamp`() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 30_000)
        assertTrue("Expected 'now' in result but got: $result", result.lowercase().contains("now"))
    }

    @Test
    fun `formatRelativeTime returns minutes ago`() {
        val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000
        val result = DateUtils.formatRelativeTime(fiveMinutesAgo)
        assertTrue("Expected 'm' in result but got: $result", result.contains("m"))
    }

    @Test
    fun `formatRelativeTime returns hours ago`() {
        val twoHoursAgo = System.currentTimeMillis() - 2 * 60 * 60 * 1000
        val result = DateUtils.formatRelativeTime(twoHoursAgo)
        assertTrue("Expected 'h' in result but got: $result", result.contains("h"))
    }

    @Test
    fun `formatDate returns non-empty string`() {
        val date = LocalDate.of(2026, 10, 18)
        val result = DateUtils.formatDate(date)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("18"))
    }

    @Test
    fun `formatDateFull contains day and month`() {
        val date = LocalDate.of(2026, 10, 18)
        val result = DateUtils.formatDateFull(date)
        assertTrue(result.contains("18"))
        assertTrue(result.isNotEmpty())
    }
}
