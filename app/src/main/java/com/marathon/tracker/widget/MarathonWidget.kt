package com.marathon.tracker.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.marathon.tracker.MainActivity
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WidgetState
import com.marathon.tracker.util.PaceFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MarathonWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),
            DpSize(270.dp, 110.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = context.dataStore
        val stateJson = dataStore.data.map { it[WidgetStateManager.WIDGET_STATE_KEY] }.first()
        val state = stateJson?.let {
            runCatching {
                Json { ignoreUnknownKeys = true }.decodeFromString<WidgetStateDto>(it).toWidgetState()
            }.getOrNull()
        }

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                if (size.width >= 200.dp) {
                    MarathonWidgetLarge(state)
                } else {
                    MarathonWidgetSmall(state)
                }
            }
        }
    }
}

@Composable
private fun MarathonWidgetSmall(state: WidgetState?) {
    val phase = state?.currentPhase ?: TrainingPhase.BASE_BUILDING
    val accentColor = ColorProvider(Color(phase.colorHex))
    val todayWorkout = state?.todayWorkout

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(modifier = GlanceModifier.fillMaxSize()) {
            // Phase accent bar
            Box(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accentColor),
            ) {}

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Date header
                val today = LocalDate.now()
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEE d MMM")).uppercase(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                    ),
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Today's run
                if (todayWorkout != null && todayWorkout.runType != RunType.REST) {
                    Text(
                        text = workoutSummaryLine(todayWorkout),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 2,
                    )
                    todayWorkout.gymSession?.let { gym ->
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = gym.focus,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                } else {
                    Text(
                        text = "Rest Day",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 13.sp,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Race countdown
                Text(
                    text = "${state?.daysToNextRace ?: "--"}d to ${state?.nextRaceName ?: "race"}",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MarathonWidgetLarge(state: WidgetState?) {
    val phase = state?.currentPhase ?: TrainingPhase.BASE_BUILDING
    val accentColor = ColorProvider(Color(phase.colorHex))
    val todayWorkout = state?.todayWorkout
    val lastActivity = state?.lastStravaActivity
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(modifier = GlanceModifier.fillMaxSize()) {
            // Phase accent bar
            Box(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accentColor),
            ) {}

            // Left column — today's workout
            Column(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width(170.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                val today = LocalDate.now()
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEE d MMM")).uppercase(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                    ),
                )

                Spacer(modifier = GlanceModifier.height(3.dp))

                if (todayWorkout != null && todayWorkout.runType != RunType.REST) {
                    Text(
                        text = workoutSummaryLine(todayWorkout),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 2,
                    )
                    todayWorkout.paceRange?.let { pace ->
                        Text(
                            text = PaceFormatter.formatPaceRange(pace),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                    todayWorkout.gymSession?.let { gym ->
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = gym.focus,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                } else {
                    Text(
                        text = "Rest Day",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Strava last activity
                if (lastActivity != null && lastActivity.type == "Run") {
                    val paceText = PaceFormatter.formatPace(lastActivity.averagePaceSecPerKm)
                    val distText = "%.1fkm".format(lastActivity.distanceKm)
                    Text(
                        text = "$distText · $paceText",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 11.sp,
                        ),
                        modifier = GlanceModifier.clickable(
                            actionStartActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("strava://activities/${lastActivity.id}"))
                            )
                        ),
                    )
                } else if (state?.isStravaConnected == false) {
                    Text(
                        text = "Connect Strava",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                    )
                }
            }

            // Right column — countdown + progress
            Column(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${state?.daysToNextRace ?: "--"}",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "days to",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                    ),
                )
                Text(
                    text = state?.nextRaceName ?: "race",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 2,
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                val done = state?.weeklyKmDone ?: 0.0
                val target = state?.weeklyKmTarget?.coerceAtLeast(1.0) ?: 1.0
                Text(
                    text = "%.0f/%.0fkm".format(done, target),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                    ),
                )
            }
        }
    }
}

private fun workoutSummaryLine(workout: DayWorkout): String {
    val type = when (workout.runType) {
        RunType.EASY -> "Easy"
        RunType.LONG -> "Long Run"
        RunType.TEMPO -> "Tempo"
        RunType.INTERVAL -> "Intervals"
        RunType.MARATHON_PACE -> "Marathon Pace"
        RunType.RECOVERY_RUN -> "Recovery"
        RunType.STRIDES -> "Strides"
        RunType.RACE -> workout.raceName ?: "RACE"
        RunType.GYM_ONLY -> "Gym"
        RunType.REST -> "Rest"
    }
    return if (workout.distanceKm > 0) "$type ${workout.distanceKm}km" else type
}

private val Context.dataStore
    get() = com.marathon.tracker.di.getDataStore(this)
