package com.marathon.tracker.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.TrainingPhase
import com.marathon.tracker.domain.model.WeekPlan
import com.marathon.tracker.presentation.theme.toColor
import com.marathon.tracker.util.PaceFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    onManagePlans: () -> Unit = {},
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val allWeeks by viewModel.allWeeks.collectAsStateWithLifecycle()
    val selectedPhase by viewModel.selectedPhase.collectAsStateWithLifecycle()
    val activePlanName by viewModel.activePlanName.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val calendarScale by viewModel.calendarScale.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val dayWorkoutMap by viewModel.dayWorkoutMap.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(allWeeks, viewMode) {
        if (viewMode == PlanViewMode.LIST) {
            val currentIndex = allWeeks.indexOfFirst { it.weekNumber == viewModel.currentWeekNumber }
            if (currentIndex >= 0) listState.scrollToItem(currentIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activePlanName) },
                actions = {
                    IconButton(onClick = onManagePlans) {
                        Icon(Icons.Default.Tune, contentDescription = "Manage Plans")
                    }
                },
            )
        },
    ) { padding ->
        if (viewMode == PlanViewMode.CALENDAR) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                ViewModeToggle(viewMode = viewMode, onModeChange = viewModel::setViewMode)
                CalendarContent(
                    scale = calendarScale,
                    selectedDate = selectedDate,
                    calendarMonth = calendarMonth,
                    dayWorkoutMap = dayWorkoutMap,
                    allWeeks = allWeeks,
                    currentWeekNumber = viewModel.currentWeekNumber,
                    onScaleChange = viewModel::setCalendarScale,
                    onDateSelect = viewModel::selectDate,
                    onNavigateMonth = viewModel::navigateMonth,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
            ) {
                item {
                    Column(Modifier.padding(top = padding.calculateTopPadding())) {
                        ViewModeToggle(viewMode = viewMode, onModeChange = viewModel::setViewMode)
                        PhaseFilterRow(selectedPhase, onSelect = viewModel::selectPhase)
                    }
                }
                items(allWeeks, key = { it.weekNumber }) { weekPlan ->
                    WeekCard(
                        weekPlan = weekPlan,
                        isCurrentWeek = weekPlan.weekNumber == viewModel.currentWeekNumber,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(viewMode: PlanViewMode, onModeChange: (PlanViewMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = viewMode == PlanViewMode.LIST,
            onClick = { onModeChange(PlanViewMode.LIST) },
            label = { Text("List") },
            leadingIcon = { Icon(Icons.Default.FormatListBulleted, null, Modifier.size(16.dp)) },
        )
        FilterChip(
            selected = viewMode == PlanViewMode.CALENDAR,
            onClick = { onModeChange(PlanViewMode.CALENDAR) },
            label = { Text("Calendar") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    scale: CalendarScale,
    selectedDate: LocalDate,
    calendarMonth: YearMonth,
    dayWorkoutMap: Map<LocalDate, DayWorkout>,
    allWeeks: List<WeekPlan>,
    currentWeekNumber: Int,
    onScaleChange: (CalendarScale) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onNavigateMonth: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Scale selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            CalendarScale.entries.forEachIndexed { index, sc ->
                SegmentedButton(
                    selected = scale == sc,
                    onClick = { onScaleChange(sc) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = CalendarScale.entries.size),
                    label = { Text(sc.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        when (scale) {
            CalendarScale.MONTH -> MonthCalendar(
                yearMonth = calendarMonth,
                today = LocalDate.now(),
                selectedDate = selectedDate,
                dayWorkoutMap = dayWorkoutMap,
                onDateSelect = onDateSelect,
                onNavigateMonth = onNavigateMonth,
            )
            CalendarScale.WEEK -> {
                val weekDays = getWeekDays(selectedDate)
                WeekCalendarView(
                    days = weekDays,
                    dayWorkoutMap = dayWorkoutMap,
                    selectedDate = selectedDate,
                    today = LocalDate.now(),
                    onDateSelect = onDateSelect,
                    onPrevWeek = { onDateSelect(selectedDate.minusWeeks(1)) },
                    onNextWeek = { onDateSelect(selectedDate.plusWeeks(1)) },
                )
            }
            CalendarScale.DAY -> DayDetailCard(
                date = selectedDate,
                workout = dayWorkoutMap[selectedDate],
                onPrevDay = { onDateSelect(selectedDate.minusDays(1)) },
                onNextDay = { onDateSelect(selectedDate.plusDays(1)) },
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun MonthCalendar(
    yearMonth: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    dayWorkoutMap: Map<LocalDate, DayWorkout>,
    onDateSelect: (LocalDate) -> Unit,
    onNavigateMonth: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Month header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { onNavigateMonth(false) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
            }
            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = { onNavigateMonth(true) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
            }
        }

        // Day of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Calendar grid
        val firstDay = yearMonth.atDay(1)
        val startOffset = (firstDay.dayOfWeek.value - 1) // 0=Mon, 6=Sun
        val daysInMonth = yearMonth.lengthOfMonth()
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayIndex = row * 7 + col - startOffset
                    if (dayIndex < 0 || dayIndex >= daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(0.75f)) {}
                    } else {
                        val date = yearMonth.atDay(dayIndex + 1)
                        val workout = dayWorkoutMap[date]
                        MonthDayCell(
                            date = date,
                            workout = workout,
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            onClick = { onDateSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    date: LocalDate,
    workout: DayWorkout?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasRun = workout != null && workout.runType != RunType.REST && workout.runType != RunType.GYM_ONLY
    val hasGym = workout?.gymSession != null
    val isRace = workout?.isRaceDay == true

    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val dayNumColor = when {
        isRace -> Color(0xFFFFB800)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dotColor = when {
        isRace -> Color(0xFFFFB800)
        hasRun -> workout!!.phase.toColor()
        hasGym -> Color(0xFF9C27B0)
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(0.75f)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "${date.dayOfMonth}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal,
            color = dayNumColor,
        )
        if (dotColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape),
            )
        }
        if (hasRun && workout!!.distanceKm > 0) {
            Text(
                text = "${workout.distanceKm.toInt()}",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeekCalendarView(
    days: List<LocalDate>,
    dayWorkoutMap: Map<LocalDate, DayWorkout>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Week navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous week")
            }
            val firstDay = days.firstOrNull() ?: today
            val lastDay = days.lastOrNull() ?: today
            Text(
                text = "${firstDay.format(DateTimeFormatter.ofPattern("d MMM"))} – ${lastDay.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onNextWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next week")
            }
        }

        // Day selector strip
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            days.forEach { date ->
                val workout = dayWorkoutMap[date]
                val isSelected = date == selectedDate
                val isToday = date == today
                val dotColor = workoutDotColor(workout)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .clickable { onDateSelect(date) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${date.dayOfMonth}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    if (dotColor != Color.Transparent) {
                        Box(Modifier.size(5.dp).background(dotColor, CircleShape))
                    }
                }
            }
        }

        // Selected day detail
        val selectedWorkout = dayWorkoutMap[selectedDate]
        if (selectedWorkout != null) {
            WorkoutDetailCard(workout = selectedWorkout)
        } else {
            Text(
                text = "No workout scheduled for ${selectedDate.format(DateTimeFormatter.ofPattern("EEE d MMM"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun DayDetailCard(
    date: LocalDate,
    workout: DayWorkout?,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous day")
            }
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onNextDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next day")
            }
        }
        if (workout != null) {
            WorkoutDetailCard(workout = workout)
        } else {
            Text(
                text = "No workout scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun WorkoutDetailCard(workout: DayWorkout) {
    val phaseColor = workout.phase.toColor()
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (workout.gymSession != null) 140.dp else 100.dp)
                    .background(phaseColor),
            )
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = workout.phase.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    if (workout.isRaceDay) {
                        Text(
                            text = "RACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFB800),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                when {
                    workout.isRaceDay -> {
                        Text(workout.raceName ?: "Race Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800))
                        Text("${workout.distanceKm} km", style = MaterialTheme.typography.bodyMedium)
                    }
                    workout.runType == RunType.REST -> {
                        Text("Rest Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    workout.runType != RunType.GYM_ONLY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, null, Modifier.padding(end = 6.dp).size(18.dp))
                            Text("${workout.runType.displayName} · ${workout.distanceKm} km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        workout.paceRange?.let { pace ->
                            Text("@ ${PaceFormatter.formatPaceRange(pace)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                workout.gymSession?.let { gym ->
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, null, Modifier.padding(end = 6.dp).size(16.dp))
                        Text("${gym.focus} · ${gym.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                    }
                }

                workout.coachNote?.let { note ->
                    Text(
                        text = "Coach: $note",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
            }
        }
    }
}

private fun workoutDotColor(workout: DayWorkout?): Color {
    if (workout == null) return Color.Transparent
    return when {
        workout.isRaceDay -> Color(0xFFFFB800)
        workout.runType == RunType.REST -> Color.Transparent
        workout.gymSession != null && workout.runType == RunType.GYM_ONLY -> Color(0xFF9C27B0)
        else -> Color(workout.phase.colorHex)
    }
}

private fun getWeekDays(date: LocalDate): List<LocalDate> {
    val monday = date.with(DayOfWeek.MONDAY)
    return (0..6).map { monday.plusDays(it.toLong()) }
}

@Composable
private fun PhaseFilterRow(selected: TrainingPhase?, onSelect: (TrainingPhase?) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") }) }
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
private fun WeekCard(weekPlan: WeekPlan, isCurrentWeek: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentWeek) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
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
                    Text(weekPlan.phase.displayName, style = MaterialTheme.typography.labelSmall, color = weekPlan.phase.toColor())
                }
                Spacer(Modifier.weight(1f))
                Text("%.0f km".format(weekPlan.totalPlannedKm), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            weekPlan.days.forEach { day -> DayRow(day) }
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
        Text(day.date.format(dayFmt), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            day.isRaceDay -> Text("RACE: ${day.raceName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800))
            day.runType == RunType.REST -> Text("Rest", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            day.runType == RunType.GYM_ONLY -> Text("Gym: ${day.gymSession?.focus ?: ""}", style = MaterialTheme.typography.bodySmall)
            else -> Row {
                Text("${day.runType.displayName} ${day.distanceKm}km", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                day.paceRange?.let { pace ->
                    Text(" @ ${PaceFormatter.formatPaceRange(pace)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                day.gymSession?.let {
                    Text(" + Gym", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
