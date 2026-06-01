package com.marathon.tracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaceFormatterTest {

    @Test
    fun `formatPace formats correctly for typical values`() {
        assertEquals("5:30/km", PaceFormatter.formatPace(330.0))
        assertEquals("6:00/km", PaceFormatter.formatPace(360.0))
        assertEquals("4:05/km", PaceFormatter.formatPace(245.0))
    }

    @Test
    fun `formatPace returns placeholder for zero`() {
        assertEquals("--:--/km", PaceFormatter.formatPace(0.0))
    }

    @Test
    fun `formatPace rounds seconds correctly`() {
        assertEquals("5:01/km", PaceFormatter.formatPace(301.0))
        assertEquals("5:59/km", PaceFormatter.formatPace(359.0))
    }

    @Test
    fun `speedMpsToSecPerKm converts correctly`() {
        // 3 m/s = 1000/3 seconds per km ≈ 333.3
        val result = PaceFormatter.speedMpsToSecPerKm(3.0)
        assertEquals(333.3, result, 0.5)
    }

    @Test
    fun `speedMpsToSecPerKm handles fast pace`() {
        // 5 m/s = 200 sec/km = 3:20/km
        val result = PaceFormatter.speedMpsToSecPerKm(5.0)
        assertEquals(200.0, result, 0.1)
    }

    @Test
    fun `speedMpsToSecPerKm returns zero for zero input`() {
        assertEquals(0.0, PaceFormatter.speedMpsToSecPerKm(0.0), 0.001)
    }

    @Test
    fun `paceDeltaText returns positive when slower than target`() {
        // actual 360 sec/km, target 330 sec/km — 30s slower
        val result = PaceFormatter.paceDeltaText(360.0, 330.0)
        assertTrue("Expected '+' in delta text: $result", result.contains("+"))
    }

    @Test
    fun `paceDeltaText returns negative when faster than target`() {
        val result = PaceFormatter.paceDeltaText(300.0, 330.0)
        assertTrue("Expected '-' in delta text: $result", result.contains("-"))
    }

    @Test
    fun `formatDuration handles hours and minutes`() {
        // 8100 seconds = 2h 15m 0s
        assertEquals("2:15:00", PaceFormatter.formatDuration(8100))
    }

    @Test
    fun `formatDuration handles sub-hour duration`() {
        // 2730 seconds = 45m 30s (no hours prefix when hours=0)
        assertEquals("45:30", PaceFormatter.formatDuration(2730))
    }

    @Test
    fun `formatPaceRange returns single value when min equals max`() {
        val range = com.marathon.tracker.domain.model.PaceRange(360, 360)
        assertEquals("6:00/km", PaceFormatter.formatPaceRange(range))
    }

    @Test
    fun `formatPaceRange returns range when min differs from max`() {
        val range = com.marathon.tracker.domain.model.PaceRange(330, 360)
        val result = PaceFormatter.formatPaceRange(range)
        assertTrue(result.contains("5:30"))
        assertTrue(result.contains("6:00"))
    }
}
