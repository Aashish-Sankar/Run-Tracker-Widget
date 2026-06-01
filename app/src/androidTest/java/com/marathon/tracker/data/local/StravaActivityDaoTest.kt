package com.marathon.tracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marathon.tracker.data.local.dao.StravaActivityDao
import com.marathon.tracker.data.local.entity.StravaActivityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StravaActivityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: StravaActivityDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.stravaActivityDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun testActivity(
        id: Long = 1L,
        name: String = "Morning Run",
        type: String = "Run",
        date: LocalDate = LocalDate.of(2026, 6, 1),
        distanceM: Double = 8000.0,
        speedMps: Double = 2.8,
        timestampMillis: Long = date.toEpochDay() * 86400L * 1000L,
    ) = StravaActivityEntity(
        id = id,
        name = name,
        type = type,
        startDateEpochDay = date.toEpochDay(),
        startDateMillis = timestampMillis,
        distanceMeters = distanceM,
        averageSpeedMps = speedMps,
        movingTimeSeconds = 2800,
        averageHeartrate = 145.0,
        maxHeartrate = 165.0,
        totalElevationGain = 50.0,
        kudosCount = 0,
        mapPolyline = null,
        syncedAtMillis = System.currentTimeMillis(),
    )

    @Test
    fun upsertAndRetrieveActivity() = runTest {
        val activity = testActivity()
        dao.upsertActivities(listOf(activity))
        val recent = dao.getRecentActivities(10).first()
        assertEquals(1, recent.size)
        assertEquals("Morning Run", recent[0].name)
    }

    @Test
    fun upsertUpdatesExistingActivity() = runTest {
        dao.upsertActivities(listOf(testActivity()))
        dao.upsertActivities(listOf(testActivity(name = "Updated Run")))
        val recent = dao.getRecentActivities(10).first()
        assertEquals(1, recent.size)
        assertEquals("Updated Run", recent[0].name)
    }

    @Test
    fun getRecentActivitiesRespectsLimit() = runTest {
        val activities = (1..10).map { i ->
            testActivity(
                id = i.toLong(),
                date = LocalDate.of(2026, 6, i),
                timestampMillis = LocalDate.of(2026, 6, i).toEpochDay() * 86400L * 1000L,
            )
        }
        dao.upsertActivities(activities)
        val recent = dao.getRecentActivities(5).first()
        assertEquals(5, recent.size)
    }

    @Test
    fun getRunForDateFindsCorrectDay() = runTest {
        val target = LocalDate.of(2026, 6, 15)
        dao.upsertActivities(listOf(
            testActivity(id = 1, date = LocalDate.of(2026, 6, 14)),
            testActivity(id = 2, date = target),
            testActivity(id = 3, date = LocalDate.of(2026, 6, 16)),
        ))
        val found = dao.getRunForDate(target.toEpochDay())
        assertNotNull(found)
        assertEquals(2L, found!!.id)
    }

    @Test
    fun getRunForDateReturnsNullWhenMissing() = runTest {
        val result = dao.getRunForDate(LocalDate.of(2099, 1, 1).toEpochDay())
        assertNull(result)
    }

    @Test
    fun getLatestActivityTimestampMillisReturnsMaxTimestamp() = runTest {
        val ts1 = 1_000_000L
        val ts2 = 5_000_000L
        val ts3 = 3_000_000L
        dao.upsertActivities(listOf(
            testActivity(id = 1, timestampMillis = ts1),
            testActivity(id = 2, timestampMillis = ts2),
            testActivity(id = 3, timestampMillis = ts3),
        ))
        val latest = dao.getLatestActivityTimestampMillis()
        assertEquals(ts2, latest)
    }
}
