package com.marathon.tracker.widget

import android.content.Context
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.assertHasText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WidgetState
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MarathonWidgetTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun testWidgetState(daysToNextRace: Int = 140, nextRaceName: String = "Half Marathon") = WidgetState(
        todayWorkout = DayWorkout(
            date = LocalDate.of(2026, 6, 1),
            weekNumber = 1,
            dayOfWeek = DayOfWeek.MONDAY,
            phase = TrainingPhase.BASE_BUILDING,
            runType = RunType.EASY,
            distanceKm = 10.0,
            paceRange = null,
            gymSession = null,
            coachNote = null,
        ),
        lastStravaActivity = null,
        daysToNextRace = daysToNextRace,
        nextRaceName = nextRaceName,
        weekNumber = 1,
        weeklyKmTarget = 42.0,
        weeklyKmDone = 18.0,
        completedRaces = emptyList(),
        isStravaConnected = false,
        lastUpdated = System.currentTimeMillis(),
        currentPhase = TrainingPhase.BASE_BUILDING,
    )

    @Test
    fun smallWidget_showsRaceCountdown() = runGlanceAppWidgetUnitTest {
        provideComposable {
            MarathonWidgetSmall(state = testWidgetState(daysToNextRace = 140))
        }
        onAllNodes().assertAny(
            assertHasText("140", substring = true)
        )
    }

    @Test
    fun smallWidget_showsWorkoutType() = runGlanceAppWidgetUnitTest {
        provideComposable {
            MarathonWidgetSmall(state = testWidgetState())
        }
        onAllNodes().assertAny(
            assertHasText("Easy", substring = true)
        )
    }

    @Test
    fun largeWidget_showsWeeklyKmProgress() = runGlanceAppWidgetUnitTest {
        provideComposable {
            MarathonWidgetLarge(state = testWidgetState())
        }
        onAllNodes().assertAny(
            assertHasText("18", substring = true)
        )
    }

    @Test
    fun largeWidget_showsRaceName() = runGlanceAppWidgetUnitTest {
        provideComposable {
            MarathonWidgetLarge(state = testWidgetState(nextRaceName = "Half Marathon"))
        }
        onAllNodes().assertAny(
            assertHasText("Half Marathon", substring = true)
        )
    }

    @Test
    fun smallWidget_handlesNullState() = runGlanceAppWidgetUnitTest {
        provideComposable {
            MarathonWidgetSmall(state = null)
        }
        // Should not crash; widget renders loading/empty state
    }
}
