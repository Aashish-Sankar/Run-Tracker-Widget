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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marathon.tracker.domain.model.HrZoneData
import com.marathon.tracker.domain.model.PaceTrendPoint
import com.marathon.tracker.domain.model.Race
import com.marathon.tracker.domain.model.WeekSummary
import com.marathon.tracker.util.DateUtils
import com.marathon.tracker.util.PaceFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val weekSummary by viewModel.weekSummary.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()
    val upcomingRaces by viewModel.upcomingRaces.collectAsStateWithLifecycle()
    val paceTrendData by viewModel.paceTrendData.collectAsStateWithLifecycle()
    val hrZoneData by viewModel.hrZoneData.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Dashboard") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            weekSummary?.let { WeeklyProgressCard(it) }
            if (recentActivities.isNotEmpty()) MiniCalendarStrip(recentActivities.map { it.startDate })
            if (upcomingRaces.isNotEmpty()) RaceCountdownCards(upcomingRaces)
            if (paceTrendData.size >= 2) PaceTrendChart(paceTrendData)
            if (hrZoneData.isNotEmpty()) HrZonesBar(hrZoneData)
        }
    }
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
                    modifier = Modifier.width(150.dp),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(race.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "$daysUntil days",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            DateUtils.formatDate(race.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                Text(
                    PaceFormatter.formatPace(points.first().paceSecPerKm),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    PaceFormatter.formatPace(points.last().paceSecPerKm),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                    Text(
                        "Z${zone.zone}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.width(4.dp))
                    LinearProgressIndicator(
                        progress = { (zone.minutes.toFloat() / total) },
                        modifier = Modifier.weight(1f),
                        color = Color(zone.colorHex),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${zone.minutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}
