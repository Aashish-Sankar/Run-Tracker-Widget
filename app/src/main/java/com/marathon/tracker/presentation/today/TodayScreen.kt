package com.marathon.tracker.presentation.today

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.domain.model.TodayWorkout
import com.marathon.tracker.presentation.theme.toColor
import com.marathon.tracker.util.DateUtils
import com.marathon.tracker.util.PaceFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(DateUtils.formatDateFull(LocalDate.now())) },
                actions = {
                    val syncing = (uiState as? TodayUiState.Success)?.isSyncing == true
                    if (syncing) CircularProgressIndicator(Modifier.padding(12.dp))
                    else IconButton(onClick = { viewModel.syncStrava() }) {
                        Icon(Icons.Default.Refresh, "Sync Strava")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is TodayUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is TodayUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(state.message, color = MaterialTheme.colorScheme.error) }

            is TodayUiState.Success -> TodayContent(
                modifier = Modifier.padding(padding),
                todayWorkout = state.todayWorkout,
                onMarkCompleted = { km, pace -> viewModel.markCompleted(km, pace) },
            )
        }
    }
}

@Composable
private fun TodayContent(
    modifier: Modifier = Modifier,
    todayWorkout: TodayWorkout,
    onMarkCompleted: (Double, Double) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        todayWorkout.plan?.let { plan ->
            WorkoutPlanCard(plan = plan, isCompleted = todayWorkout.isCompleted)
        } ?: Text("No workout planned for today.", style = MaterialTheme.typography.bodyLarge)

        todayWorkout.matchedStravaActivity?.let { activity ->
            StravaMatchCard(
                activity = activity,
                plan = todayWorkout.plan,
            )
        }

        if (!todayWorkout.isCompleted && todayWorkout.plan?.runType != RunType.REST) {
            var showDialog by remember { mutableStateOf(false) }
            FilledTonalButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.CheckCircle, null, Modifier.padding(end = 8.dp))
                Text("Mark as Completed")
            }
            if (showDialog) {
                MarkCompletedDialog(
                    plannedKm = todayWorkout.plan?.distanceKm ?: 0.0,
                    onConfirm = { km, pace ->
                        onMarkCompleted(km, pace)
                        showDialog = false
                    },
                    onDismiss = { showDialog = false },
                )
            }
        }

        todayWorkout.plan?.coachNote?.let { note ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Coach: $note",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
private fun WorkoutPlanCard(plan: DayWorkout, isCompleted: Boolean) {
    val phaseColor = plan.phase.toColor()

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .background(phaseColor)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.phase.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    if (isCompleted) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (plan.runType != RunType.REST && plan.runType != RunType.GYM_ONLY) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsRun, null, Modifier.padding(end = 6.dp))
                        Text(
                            text = "${plan.runType.displayName} ${plan.distanceKm}km",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    plan.paceRange?.let { pace ->
                        Text(
                            text = "@ ${PaceFormatter.formatPaceRange(pace)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                plan.gymSession?.let { gym ->
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, null, Modifier.padding(end = 6.dp))
                        Text(
                            text = "${gym.focus} · ${gym.durationMinutes} min",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (plan.runType == RunType.REST) {
                    Text("Rest Day", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun StravaMatchCard(activity: StravaActivity, plan: DayWorkout?) {
    val paceText = PaceFormatter.formatPace(activity.averagePaceSecPerKm)
    val distText = "%.2f km".format(activity.distanceKm)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(end = 6.dp))
                Text("Strava match: ${activity.name}", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(4.dp))
            Text("$distText · $paceText", style = MaterialTheme.typography.bodyMedium)
            activity.averageHeartrate?.let {
                Text("${it.toInt()} bpm avg HR", style = MaterialTheme.typography.bodySmall)
            }
            plan?.paceRange?.let { target ->
                val delta = PaceFormatter.paceDeltaText(activity.averagePaceSecPerKm, target.midpointSecondsPerKm.toDouble())
                Text(
                    text = "vs target: $delta",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activity.averagePaceSecPerKm <= target.maxSecondsPerKm)
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                )
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

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log this workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it },
                    label = { Text("Distance (km)") },
                )
                androidx.compose.material3.OutlinedTextField(
                    value = paceText,
                    onValueChange = { paceText = it },
                    label = { Text("Avg pace (sec/km, e.g. 390)") },
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val km = kmText.toDoubleOrNull() ?: plannedKm
                val pace = paceText.toDoubleOrNull() ?: 390.0
                onConfirm(km, pace)
            }) { Text("Save") }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
