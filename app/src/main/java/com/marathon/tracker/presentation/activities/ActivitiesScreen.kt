package com.marathon.tracker.presentation.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marathon.tracker.domain.model.StravaActivity
import com.marathon.tracker.util.DateUtils
import com.marathon.tracker.util.PaceFormatter
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(viewModel: ActivitiesViewModel = hiltViewModel()) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activities") },
                actions = {
                    if (isSyncing) CircularProgressIndicator(Modifier.padding(12.dp))
                    else IconButton(onClick = { viewModel.sync() }) {
                        Icon(Icons.Default.Refresh, "Sync")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding),
        ) {
            if (activities.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsRun, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("No activities yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (lastSyncTime > 0) {
                            Text(
                                "Last synced ${DateUtils.formatRelativeTime(lastSyncTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (lastSyncTime > 0) {
                        item {
                            Text(
                                "Last synced ${DateUtils.formatRelativeTime(lastSyncTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                    items(activities, key = { it.id }) { activity ->
                        ActivityCard(activity)
                    }
                }
            }
        }
    }
}

private val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d")

@Composable
private fun ActivityCard(activity: StravaActivity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.DirectionsRun,
                null,
                tint = if (activity.type == "Run") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(activity.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(activity.startDate.format(dateFmt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("%.2f km".format(activity.distanceKm), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(PaceFormatter.formatPace(activity.averagePaceSecPerKm), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                activity.averageHeartrate?.let {
                    Text("${it.toInt()} bpm", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE53935))
                }
            }
        }
    }
}
