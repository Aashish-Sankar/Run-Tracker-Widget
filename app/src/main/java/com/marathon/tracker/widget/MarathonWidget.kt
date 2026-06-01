package com.marathon.tracker.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private fun phaseHeaderColor(phase: TrainingPhase) = ColorProvider(Color(phase.colorHex))
private val onPhaseText = ColorProvider(Color(0xFF1C1C1E))
private val stravaOrange = ColorProvider(Color(0xFFFC4C02))
private val progressEmpty = ColorProvider(Color(0xFF3A3A3C))

@Composable
internal fun MarathonWidgetSmall(state: WidgetState?) {
    val phase = state?.currentPhase ?: TrainingPhase.BASE_BUILDING
    val todayWorkout = state?.todayWorkout
    val context = LocalContext.current
    val today = LocalDate.now()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Phase header strip
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(phaseHeaderColor(phase))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = phase.displayName.uppercase(),
                        style = TextStyle(color = onPhaseText, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    state?.weekNumber?.let {
                        Text(
                            text = "WK $it",
                            style = TextStyle(color = onPhaseText, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEE d MMM")).uppercase(),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp),
                )
                Spacer(GlanceModifier.height(2.dp))

                when {
                    todayWorkout != null && todayWorkout.isRaceDay -> {
                        Text(
                            text = "RACE DAY",
                            style = TextStyle(color = stravaOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = todayWorkout.raceName ?: "",
                            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 10.sp),
                            maxLines = 1,
                        )
                    }
                    todayWorkout != null && todayWorkout.runType != RunType.REST && todayWorkout.runType != RunType.GYM_ONLY -> {
                        Text(
                            text = todayWorkout.runType.displayName,
                            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                        )
                        Text(
                            text = "${todayWorkout.distanceKm} km",
                            style = TextStyle(color = stravaOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                    todayWorkout?.runType == RunType.GYM_ONLY -> {
                        Text(
                            text = "Gym Day",
                            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        )
                        todayWorkout.gymSession?.let {
                            Text(text = it.focus, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp), maxLines = 1)
                        }
                    }
                    else -> {
                        Text(
                            text = "Rest Day",
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
                        )
                    }
                }

                Spacer(GlanceModifier.defaultWeight())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state?.daysToNextRace ?: "--"}d",
                        style = TextStyle(color = stravaOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = state?.nextRaceName ?: "to race",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MarathonWidgetLarge(state: WidgetState?) {
    val phase = state?.currentPhase ?: TrainingPhase.BASE_BUILDING
    val todayWorkout = state?.todayWorkout
    val lastActivity = state?.lastStravaActivity
    val context = LocalContext.current
    val today = LocalDate.now()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Phase header strip
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(phaseHeaderColor(phase))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = phase.displayName.uppercase(),
                        style = TextStyle(color = onPhaseText, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    state?.weekNumber?.let {
                        Text(
                            text = "WEEK $it",
                            style = TextStyle(color = onPhaseText, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                // Left column: today's workout
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                ) {
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("EEE, d MMM")).uppercase(),
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp),
                    )
                    Spacer(GlanceModifier.height(3.dp))

                    if (todayWorkout != null && todayWorkout.runType != RunType.REST) {
                        Text(
                            text = workoutSummaryLine(todayWorkout),
                            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                        )
                        todayWorkout.paceRange?.let { pace ->
                            Text(
                                text = PaceFormatter.formatPaceRange(pace),
                                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp),
                            )
                        }
                        todayWorkout.gymSession?.let { gym ->
                            Text(
                                text = "+ ${gym.focus}",
                                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp),
                                maxLines = 1,
                            )
                        }
                    } else {
                        Text(
                            text = "Rest Day",
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
                        )
                    }

                    Spacer(GlanceModifier.defaultWeight())

                    if (lastActivity != null && lastActivity.type == "Run") {
                        val paceText = PaceFormatter.formatPace(lastActivity.averagePaceSecPerKm)
                        val distText = "%.1fkm".format(lastActivity.distanceKm)
                        Text(
                            text = "Strava: $distText · $paceText",
                            style = TextStyle(color = stravaOrange, fontSize = 10.sp, fontWeight = FontWeight.Medium),
                            modifier = GlanceModifier.clickable(
                                actionStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse("strava://activities/${lastActivity.id}")))
                            ),
                        )
                    }
                }

                // Divider
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .padding(vertical = 2.dp)
                        .background(GlanceTheme.colors.outline),
                ) {}

                // Right column: countdown + weekly progress
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state?.daysToNextRace ?: "--"}",
                        style = TextStyle(color = stravaOrange, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "DAYS TO",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 8.sp),
                    )
                    Text(
                        text = state?.nextRaceName ?: "next race",
                        style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Medium),
                        maxLines = 2,
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    val done = state?.weeklyKmDone ?: 0.0
                    val target = state?.weeklyKmTarget?.coerceAtLeast(1.0) ?: 1.0
                    Text(
                        text = "${done.toInt()}/${target.toInt()} km",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp),
                    )
                    Spacer(GlanceModifier.height(3.dp))
                    GlanceProgressBar(done = done, target = target)
                }
            }
        }
    }
}

@Composable
private fun GlanceProgressBar(done: Double, target: Double) {
    val filled = ((done / target.coerceAtLeast(1.0)) * 8).toInt().coerceIn(0, 8)
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        repeat(8) { i ->
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(5.dp)
                    .padding(end = if (i < 7) 1.dp else 0.dp)
                    .background(if (i < filled) stravaOrange else progressEmpty),
            ) {}
        }
    }
}

private fun workoutSummaryLine(workout: DayWorkout): String {
    val type = when (workout.runType) {
        RunType.EASY -> "Easy"
        RunType.LONG -> "Long Run"
        RunType.TEMPO -> "Tempo"
        RunType.INTERVAL -> "Intervals"
        RunType.MARATHON_PACE -> "MP Run"
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
