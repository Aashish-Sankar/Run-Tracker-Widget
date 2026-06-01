package com.marathon.tracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marathon.tracker.data.local.dao.WorkoutLogDao
import com.marathon.tracker.data.local.entity.WorkoutLogEntity
import com.marathon.tracker.domain.model.RunType
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
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class WorkoutLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WorkoutLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.workoutLogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun testLog(
        date: LocalDate = LocalDate.of(2026, 6, 1),
        weekNumber: Int = 1,
        isCompleted: Boolean = true,
        distanceKm: Double = 8.0,
        paceSecPerKm: Double = 360.0,
    ) = WorkoutLogEntity(
        id = UUID.randomUUID().toString(),
        dateEpochDay = date.toEpochDay(),
        weekNumber = weekNumber,
        plannedDistanceKm = 8.0,
        plannedRunType = RunType.EASY.name,
        actualDistanceKm = distanceKm,
        actualPaceSecPerKm = paceSecPerKm,
        stravaActivityId = null,
        isCompleted = isCompleted,
        notes = null,
        completedAtMillis = if (isCompleted) System.currentTimeMillis() else null,
    )

    @Test
    fun upsertAndRetrieveLog() = runTest {
        val log = testLog()
        dao.upsertLog(log)
        val retrieved = dao.getLogForDate(log.dateEpochDay)
        assertNotNull(retrieved)
        assertEquals(8.0, retrieved!!.actualDistanceKm!!, 0.001)
    }

    @Test
    fun upsertUpdatesExistingLog() = runTest {
        val log = testLog()
        dao.upsertLog(log)
        val updated = log.copy(actualDistanceKm = 10.0)
        dao.upsertLog(updated)
        val retrieved = dao.getLogForDate(log.dateEpochDay)
        assertEquals(10.0, retrieved!!.actualDistanceKm!!, 0.001)
    }

    @Test
    fun getLogForDateReturnsNullWhenMissing() = runTest {
        val result = dao.getLogForDate(LocalDate.of(2099, 1, 1).toEpochDay())
        assertNull(result)
    }

    @Test
    fun getWeeklyActualKmSumsCorrectly() = runTest {
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 1), weekNumber = 1, distanceKm = 8.0))
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 2), weekNumber = 1, distanceKm = 12.0))
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 3), weekNumber = 1, distanceKm = 10.0))
        val total = dao.getWeeklyActualKm(1).first()
        assertEquals(30.0, total, 0.001)
    }

    @Test
    fun getCompletedDaysInWeekCountsOnlyCompleted() = runTest {
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 1), weekNumber = 1, isCompleted = true))
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 2), weekNumber = 1, isCompleted = false))
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 3), weekNumber = 1, isCompleted = true))
        val count = dao.getCompletedDaysInWeek(1).first()
        assertEquals(2, count)
    }

    @Test
    fun getLogsForWeekReturnsAllLogsForWeek() = runTest {
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 1), weekNumber = 1))
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 2), weekNumber = 1))
        dao.upsertLog(testLog(date = LocalDate.of(2026, 6, 8), weekNumber = 2))
        val logs = dao.getLogsForWeek(1).first()
        assertEquals(2, logs.size)
    }
}
