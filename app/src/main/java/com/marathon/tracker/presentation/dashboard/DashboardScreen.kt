package com.marathon.tracker.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.HrZoneData
import com.marathon.tracker.domain.model.PaceTrendPoint
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.domain.model.WeekSummary
import com.marathon.tracker.presentation.theme.toColor
import com.marathon.tracker.util.DateUtils
import com.marathon.tracker.util.PaceFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val todayWorkout by viewModel.todayWorkout.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val weekSummary by viewModel.weekSummary.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()
    val upcomingRaces by viewModel.upcomingRaces.collectAsStateWithLifecycle()
    val paceTrendData by viewModel.paceTrendData.collectAsStateWithLifecycle()
    val hrZoneData by viewModel.hrZoneData.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(24.dp))
                    } else {
                        IconButton(onClick = { viewModel.syncStrava() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Strava")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Today's workout at the top
            TodayWorkoutSection(
                todayWorkout = todayWorkout,
                onMarkCompleted = viewModel::markCompleted,
            )
            weekSummary?.let { WeeklyProgressCard(it) }
            if (recentActivities.isNotEmpty()) MiniCalendarStrip(recentActivities.map { it.startDate })
            if (upcomingRaces.isNotEmpty()) RaceCountdownCards(upcomingRaces)
            if (paceTrendData.size >= 2) PaceTrendChart(paceTrendData)
            if (hrZoneData.isNotEmpty()) HrZonesBar(hrZoneData)
        }
    }
}

@Composable
private fun TodayWorkoutSection(
    todayWorkout: TodayWorkout?,
    onMarkCompleted: (Double, Double) -> Unit,
) {
    val plan = todayWorkout?.plan
    val phaseColor = plan?.phase?.toColor() ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Phase accent top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(phaseColor),
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = DateUtils.formatDateFull(LocalDate.now()),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    plan?.phase?.let { phase ->
                        Text(
                            text = phase.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.shapes.small,
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                if (plan == null) {
                    Text("No workout loaded yet.", style = MaterialTheme.typography.bodyMedium)
                } else if (plan.runType == RunType.REST) {
                    Text("Rest Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                } else {
                    if (plan.runType != RunType.GYM_ONLY) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, null, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = "${plan.runType.displayName}  ${plan.distanceKm} km",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (todayWorkout?.isCompleted == true) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                            }
                        }
                        plan.paceRange?.let { pace ->
                            Text(
                                text = "@ ${PaceFormatter.formatPaceRange(pace)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    plan.gymSession?.let { gym ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.padding(end = 6.dp).size(16.dp))
                            Text(text = "${gym.focus} · ${gym.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    plan.coachNote?.let { note ->
                        Text(
                            text = "Coach: $note",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Mark completed button
                if (todayWorkout?.isCompleted == false && plan?.runType != RunType.REST) {
                    var showDialog by remember { mutableStateOf(false) }
                    FilledTonalButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.padding(end = 6.dp))
                        Text("Mark as Completed")
                    }
                    if (showDialog) {
                        MarkCompletedDialog(
                            plannedKm = plan?.distanceKm ?: 0.0,
                            onConfirm = { km, pace ->
                                onMarkCompleted(km, pace)
                                showDialog = false
                            },
                            onDismiss = { showDialog = false },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkCompletedDialog(
    plannedKm: Double,
    onConfirm: (Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var kmText by remember { mutableStateOf(plannedKm.toString()) }
    var paceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log this workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = kmText, onValueChange = { kmText = it }, label = { Text("Distance (km)") })
                OutlinedTextField(value = paceText, onValueChange = { paceText = it }, label = { Text("Avg pace (sec/km, e.g. 390)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val km = kmText.toDoubleOrNull() ?: plannedKm
                val pace = paceText.toDoubleOrNull() ?: 390.0
                onConfirm(km, pace)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun WeeklyProgressCard(summary: WeekSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Week ${summary.weekNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Done", "%.1f km".format(summary.actualKm))
                StatItem("Target", "%.1f km".format(summary.plannedKm))
                StatItem("Days", "${summary.completedDays}/${summary.totalDays}")
            }
            val progress = if (summary.plannedKm > 0) (summary.actualKm / summary.plannedKm).coerceIn(0.0, 1.0) else 0.0
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = if (progress >= 1.0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            )
            Text(
                "%.0f%% of weekly target".format(progress * 100),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MiniCalendarStrip(dates: List<LocalDate>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Recent Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val today = LocalDate.now()
                val last14 = (13 downTo 0).map { today.minusDays(it.toLong()) }
                items(last14) { date ->
                    val hasActivity = dates.any { it == date }
                    val isToday = date == today
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                when {
                                    isToday -> MaterialTheme.colorScheme.primary
                                    hasActivity -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                MaterialTheme.shapes.small,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${date.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday || hasActivity) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceCountdownCards(races: List<Race>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Upcoming Races", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(races) { race ->
                val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), race.date)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.width(160.dp),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(race.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "$daysUntil days",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(DateUtils.formatDate(race.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        race.distanceKm.let {
                            Text("${it}km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaceTrendChart(points: List<PaceTrendPoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Pace Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            val lineColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                val paces = points.map { it.paceSecPerKm.toFloat() }
                if (paces.size < 2) return@Canvas
                val minPace = paces.min()
                val maxPace = paces.max()
                val range = (maxPace - minPace).coerceAtLeast(30f)
                val stepX = size.width / (paces.size - 1)
                val path = Path()
                paces.forEachIndexed { i, pace ->
                    val x = i * stepX
                    val y = size.height - ((pace - minPace) / range) * size.height
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, lineColor, style = Stroke(width = 3f))
                paces.forEachIndexed { i, pace ->
                    val x = i * stepX
                    val y = size.height - ((pace - minPace) / range) * size.height
                    drawCircle(lineColor, radius = 6f, center = Offset(x, y))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(PaceFormatter.formatPace(points.first().paceSecPerKm), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(PaceFormatter.formatPace(points.last().paceSecPerKm), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HrZonesBar(zones: List<HrZoneData>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("HR Zone Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val total = zones.sumOf { it.minutes }.coerceAtLeast(1)
            zones.forEach { zone ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Z${zone.zone}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Spacer(Modifier.width(4.dp))
                    LinearProgressIndicator(
                        progress = { zone.minutes.toFloat() / total },
                        modifier = Modifier.weight(1f),
                        color = Color(zone.colorHex),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${zone.minutes}m", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}
