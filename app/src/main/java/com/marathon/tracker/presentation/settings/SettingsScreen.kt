package com.marathon.tracker.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.marathon.tracker.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val refreshInterval by viewModel.widgetRefreshInterval.collectAsStateWithLifecycle()
    val isStravaConnected by viewModel.isStravaConnected.collectAsStateWithLifecycle()
    val athleteName by viewModel.athleteName.collectAsStateWithLifecycle()
    val athleteAvatarUrl by viewModel.athleteAvatarUrl.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var claudeKey by remember { mutableStateOf(viewModel.getClaudeApiKey()) }
    var claudeKeyDirty by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Strava section
            androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Strava", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (isStravaConnected) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (athleteAvatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = athleteAvatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                )
                            }
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(athleteName, fontWeight = FontWeight.Medium)
                                Text("Connected", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                            }
                        }
                        Button(
                            onClick = { viewModel.disconnectStrava() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Disconnect Strava") }
                    } else {
                        Text("Connect Strava to sync activities", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { (context as? ComponentActivity)?.let { viewModel.launchStravaAuth(it) } },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Connect Strava") }
                    }
                }
            }

            // Theme section
            androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            ) {
                                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                }
            }

            // Widget refresh interval
            androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Widget Refresh Interval", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    RefreshIntervalDropdown(
                        selected = refreshInterval,
                        onSelect = viewModel::setRefreshInterval,
                    )
                }
            }

            // Claude API key
            androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Claude AI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = claudeKey,
                        onValueChange = { claudeKey = it; claudeKeyDirty = true },
                        label = { Text("Claude API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    if (claudeKeyDirty) {
                        Button(onClick = {
                            viewModel.saveClaudeApiKey(claudeKey)
                            claudeKeyDirty = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Save API Key")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshIntervalDropdown(selected: Int, onSelect: (Int) -> Unit) {
    val options = listOf(15 to "15 minutes", 30 to "30 minutes", 60 to "1 hour", 120 to "2 hours")
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: "30 minutes"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { onSelect(value); expanded = false },
                )
            }
        }
    }
}
