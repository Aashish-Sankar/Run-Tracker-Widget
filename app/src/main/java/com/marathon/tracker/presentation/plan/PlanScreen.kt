package com.marathon.tracker.presentation.plan

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.presentation.theme.toColor
import com.marathon.tracker.util.PaceFormatter
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: PlanViewModel = hiltViewModel()) {
    val allWeeks by viewModel.allWeeks.collectAsStateWithLifecycle()
    val selectedPhase by viewModel.selectedPhase.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(allWeeks) {
        val currentIndex = allWeeks.indexOfFirst { it.weekNumber == viewModel.currentWeekNumber }
        if (currentIndex >= 0) listState.scrollToItem(currentIndex)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Training Plan") }) }) { padding ->
        Column(Modifier.padding(padding)) {
            PhaseFilterRow(selectedPhase, onSelect = viewModel::selectPhase)
            LazyColumn(state = listState, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(allWeeks, key = { it.weekNumber }) { weekPlan ->
                    WeekCard(weekPlan, isCurrentWeek = weekPlan.weekNumber == viewModel.currentWeekNumber)
                }
            }
        }
    }
}

@Composable
private fun PhaseFilterRow(selected: TrainingPhase?, onSelect: (TrainingPhase?) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        items(TrainingPhase.entries) { phase ->
            FilterChip(
                selected = selected == phase,
                onClick = { onSelect(if (selected == phase) null else phase) },
                label = { Text(phase.displayName) },
            )
        }
    }
}

@Composable
private fun WeekCard(weekPlan: WeekPlan, isCurrentWeek: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentWeek) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
        border = if (isCurrentWeek) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(weekPlan.phase.toColor(), MaterialTheme.shapes.small),
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Week ${weekPlan.weekNumber}${if (isCurrentWeek) " · Current" else ""}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        weekPlan.phase.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = weekPlan.phase.toColor(),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "%.0f km".format(weekPlan.totalPlannedKm),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            weekPlan.days.forEach { day ->
                DayRow(day)
            }
        }
    }
}

private val dayFmt = DateTimeFormatter.ofPattern("EEE d MMM")

@Composable
private fun DayRow(day: DayWorkout) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            day.date.format(dayFmt),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(80.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when {
            day.isRaceDay -> Text(
                "RACE: ${day.raceName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            day.runType == RunType.REST -> Text(
                "Rest",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            day.runType == RunType.GYM_ONLY -> Text(
                "Gym: ${day.gymSession?.focus ?: ""}",
                style = MaterialTheme.typography.bodySmall,
            )
            else -> Row {
                Text(
                    "${day.runType.displayName} ${day.distanceKm}km",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
                day.paceRange?.let { pace ->
                    Text(
                        " @ ${PaceFormatter.formatPaceRange(pace)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                day.gymSession?.let {
                    Text(" + Gym", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
