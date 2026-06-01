package com.marathon.tracker.presentation.coaching

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.marathon.tracker.domain.model.CoachingReport
import com.marathon.tracker.domain.model.RaceReadiness

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingScreen(viewModel: CoachingViewModel = hiltViewModel()) {
    val report by viewModel.report.collectAsStateWithLifecycle()
    val canGenerate by viewModel.canGenerate.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("AI Coaching") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (canGenerate || report == null) {
                Button(
                    onClick = { viewModel.generateReport() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating && canGenerate,
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                    }
                    Text(if (isGenerating) "Generating…" else "Generate Weekly Report")
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (report == null && !isGenerating) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No coaching report yet.\nGenerate your first weekly report!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            report?.let { CoachingReportContent(it) }
        }
    }
}

@Composable
private fun CoachingReportContent(report: CoachingReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "\"${report.weeklyMotivation}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "Week ${report.weekNumber} Report",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }

    CollapsibleSection("Summary") {
        Text(report.summary, style = MaterialTheme.typography.bodyMedium)
    }

    if (report.highlights.isNotEmpty()) {
        CollapsibleSection("Highlights") {
            report.highlights.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }

    if (report.concerns.isNotEmpty()) {
        CollapsibleSection("Areas to Watch") {
            report.concerns.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        }
    }

    CollapsibleSection("Pace Analysis") {
        Text(report.paceAnalysis, style = MaterialTheme.typography.bodyMedium)
    }

    if (report.nextWeekFocus.isNotEmpty()) {
        CollapsibleSection("Next Week Focus") {
            report.nextWeekFocus.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }

    if (report.raceReadiness.isNotEmpty()) {
        CollapsibleSection("Race Readiness") {
            report.raceReadiness.forEach { (raceName, readiness) ->
                RaceReadinessBar(raceName, readiness)
            }
        }
    }
}

@Composable
private fun CollapsibleSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun RaceReadinessBar(raceName: String, readiness: RaceReadiness) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(raceName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text("${readiness.score}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { readiness.score / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = when {
                readiness.score >= 80 -> Color(0xFF4CAF50)
                readiness.score >= 60 -> Color(0xFFFFC107)
                else -> Color(0xFFFF5722)
            },
        )
        Text(readiness.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
