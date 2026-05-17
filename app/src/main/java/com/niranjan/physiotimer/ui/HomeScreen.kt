package com.niranjan.physiotimer.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niranjan.physiotimer.data.AppSettings
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseRepository
import com.niranjan.physiotimer.data.SessionRecord
import com.niranjan.physiotimer.data.MotivationVoiceOption
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
private const val STREAK_THRESHOLD_SECONDS = 600  // 10 minutes

@Composable
internal fun HomeScreen(
    repository: ExerciseRepository,
    selectedTab: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    completionMarkerResetAt: Long,
    onResetCompletionMarkers: () -> Unit,
    expandedExerciseKey: String?,
    onExpandedExerciseKeyChange: (String?) -> Unit,
    onCreate: () -> Unit,
    onEdit: (Exercise) -> Unit,
    onStart: (Exercise) -> Unit
) {
    val savedExercisesState by remember(repository) {
        repository.observeExercises().map { exercises -> exercises as List<Exercise>? }
    }.collectAsState(initial = null)
    val sessionRecordsState by remember(repository) {
        repository.observeSessionRecords().map { sessions -> sessions as List<SessionRecord>? }
    }.collectAsState(initial = null)
    val isLoading = savedExercisesState == null
    val exercises = savedExercisesState.orEmpty()
    val completedKeySet = remember(sessionRecordsState, completionMarkerResetAt) {
        completedRoutineKeys(sessionRecordsState.orEmpty(), completionMarkerResetAt)
    }
    val hasCompletedMarkers = completedKeySet.isNotEmpty()
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<Exercise?>(null) }
    var pendingResetCompleted by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filteredExercises = remember(exercises, query) { exercises.filterByQuery(query) }
    val incompleteExercises = remember(filteredExercises, completedKeySet) {
        filteredExercises.filterNot { exercise ->
            completedKeySet.contains(exerciseCompletionKey(exercise))
        }
    }
    val completedExercises = remember(filteredExercises, completedKeySet) {
        filteredExercises.filter { exercise ->
            completedKeySet.contains(exerciseCompletionKey(exercise))
        }
    }
    val streakData = remember(sessionRecordsState) {
        computeStreakData(sessionRecordsState.orEmpty())
    }
    WellnessScreen {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { PhysioBottomBar(selectedTab, onTabSelected) },
            floatingActionButton = { CreateRoutineFab(onClick = onCreate) },
            floatingActionButtonPosition = FabPosition.End
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = WellnessSpacing.Lg),
                contentPadding = PaddingValues(bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(WellnessSpacing.Md)
            ) {
                item {
                    WellnessHeader(
                        action = {
                            HeaderResetButton(onClick = { pendingResetCompleted = true })
                        }
                    )
                }

                item {
                    WellnessSearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search routines"
                    )
                }

                if (query.isBlank()) {
                    item {
                        StreakGraphCard(streakData = streakData)
                    }
                }

                if (isLoading) {
                    item {
                        EmptyPanel(
                            title = "Loading routines",
                            message = "Preparing your saved recovery flows."
                        )
                    }
                } else if (incompleteExercises.isEmpty() && completedExercises.isEmpty()) {
                    item {
                        EmptyPanel(
                            title = if (exercises.isEmpty()) "Start with one gentle routine" else "No matching routines",
                            message = if (exercises.isEmpty()) {
                                "Create a simple guided flow for your rehab exercises."
                            } else {
                                "Try a different search, or create a new routine."
                            }
                        )
                    }
                }

                if (incompleteExercises.isNotEmpty()) {
                    item {
                        ActiveRoutinePanel(
                            exercises = incompleteExercises,
                            expandedExerciseKey = expandedExerciseKey,
                            onExpandedExerciseKeyChange = onExpandedExerciseKeyChange,
                            onStart = onStart,
                            onEdit = onEdit,
                            onDelete = { pendingDelete = it }
                        )
                    }
                }

                if (completedExercises.isNotEmpty()) {
                    item {
                        SectionLabel(
                            title = "Completed",
                            subtitle = "Completed routines stay at the bottom."
                        )
                    }
                    items(completedExercises, key = { "${it.id}-${it.name}" }) { exercise ->
                        val exerciseKey = exerciseCompletionKey(exercise)
                        CompletedRoutineRow(
                            exercise = exercise,
                            onStart = { onStart(exercise) },
                            onEdit = { onEdit(exercise) },
                            onDelete = { pendingDelete = exercise },
                            onToggleExpand = {
                                onExpandedExerciseKeyChange(
                                    if (expandedExerciseKey == exerciseKey) null else exerciseKey
                                )
                            },
                            isExpanded = expandedExerciseKey == exerciseKey
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { exercise ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = WellnessSurfaces.Card,
            title = { Text("Delete routine?") },
            text = { Text("\"${exercise.name}\" and its steps will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repository.deleteExercise(exercise.id) }
                        pendingDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.tertiary) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (pendingResetCompleted) {
        AlertDialog(
            onDismissRequest = { pendingResetCompleted = false },
            containerColor = WellnessSurfaces.Card,
            title = { Text(if (hasCompletedMarkers) "Reset completed marks?" else "No completed marks") },
            text = {
                Text(
                    if (hasCompletedMarkers) {
                        "This clears only card highlights. Session history in Progress stays unchanged."
                    } else {
                        "Complete a routine to see completed highlights on cards."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (hasCompletedMarkers) {
                            onResetCompletionMarkers()
                        }
                        pendingResetCompleted = false
                    }
                ) {
                    Text(if (hasCompletedMarkers) "Reset" else "OK")
                }
            },
            dismissButton = if (hasCompletedMarkers) {
                {
                    TextButton(onClick = { pendingResetCompleted = false }) {
                        Text("Cancel")
                    }
                }
            } else {
                null
            }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun ProgressScreen(
    selectedTab: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    repository: ExerciseRepository,
    completionMarkerResetAt: Long,
    onResetCompletionMarkers: () -> Unit,
    onStart: (Exercise) -> Unit,
    onEdit: (Exercise) -> Unit
) {
    val sessionRecordsState by remember(repository) {
        repository.observeSessionRecords().map { sessions -> sessions as List<SessionRecord>? }
    }.collectAsState(initial = null)
    val isLoading = sessionRecordsState == null
    val sessions = remember(sessionRecordsState) {
        sessionRecordsState.orEmpty()
            .filter { it.startedAt > 0L }
            .sortedByDescending { it.startedAt }
    }
    val progressSummary = remember(sessions) { computeProgressSummary(sessions) }
    val report = remember(sessions) { computeProgressReport(sessions) }
    val weekly = remember(sessions) { computeSevenDayActivity(sessions) }
    val recentSessions = remember(sessions) { sessions.take(8) }
    val hasCompletedMarkers = remember(sessions, completionMarkerResetAt) {
        sessions.any { session ->
            session.completed && (if (session.endedAt > 0L) session.endedAt else session.startedAt) > completionMarkerResetAt
        }
    }
    var pendingResetCompleted by remember { mutableStateOf(false) }

    WellnessScreen {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { PhysioBottomBar(selectedTab, onTabSelected) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = WellnessSpacing.Lg),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(WellnessSpacing.Md)
            ) {
                item {
                    WellnessHeader(
                        action = {
                            HeaderResetButton(onClick = { pendingResetCompleted = true })
                        }
                    )
                }

                if (!isLoading) {
                    item {
                        ProgressOverview(summary = progressSummary)
                    }
                    item { ProgressHighlights(report = report) }
                    item { CompletionRingReport(report = report) }
                    item { SevenDayActivityReport(weekly = weekly) }
                }

                if (isLoading) {
                    item {
                        EmptyPanel(
                            title = "Loading report",
                            message = "We are preparing your recovery analytics."
                        )
                    }
                } else if (sessions.isEmpty()) {
                    item {
                        EmptyPanel(
                            title = "No progress yet",
                            message = "Complete at least one session to unlock report insights."
                        )
                    }
                }

                if (!isLoading && recentSessions.isNotEmpty()) {
                    item {
                        RecentSessionReportCard(sessions = recentSessions)
                    }
                }
            }
        }
    }

    if (pendingResetCompleted) {
        AlertDialog(
            onDismissRequest = { pendingResetCompleted = false },
            containerColor = WellnessSurfaces.Card,
            title = { Text(if (hasCompletedMarkers) "Reset completed marks?" else "No completed marks") },
            text = {
                Text(
                    if (hasCompletedMarkers) {
                        "This clears only card highlights. Session history in Progress stays unchanged."
                    } else {
                        "Complete a routine to see completed highlights on cards."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (hasCompletedMarkers) {
                            onResetCompletionMarkers()
                        }
                        pendingResetCompleted = false
                    }
                ) {
                    Text(if (hasCompletedMarkers) "Reset" else "OK")
                }
            },
            dismissButton = if (hasCompletedMarkers) {
                {
                    TextButton(onClick = { pendingResetCompleted = false }) {
                        Text("Cancel")
                    }
                }
            } else {
                null
            }
        )
    }
}

@Composable
internal fun SettingsScreen(
    selectedTab: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    repository: ExerciseRepository,
    appSettings: AppSettings,
    onResetCompletionMarkers: () -> Unit,
    onVoiceCuesChange: (Boolean) -> Unit,
    onVibrationFeedbackChange: (Boolean) -> Unit,
    onSpeakCountdownDefaultChange: (Boolean) -> Unit,
    onMotivationChange: (Boolean) -> Unit,
    onMotivationVoiceChange: (MotivationVoiceOption) -> Unit,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    onLargerTimerTextChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit
) {
    val sessionRecordsState by remember(repository) {
        repository.observeSessionRecords().map { sessions -> sessions as List<SessionRecord>? }
    }.collectAsState(initial = null)
    val hasCompletedMarkers = remember(sessionRecordsState, appSettings.completionMarkerResetAt) {
        sessionRecordsState.orEmpty().any { session ->
            session.completed && (if (session.endedAt > 0L) session.endedAt else session.startedAt) > appSettings.completionMarkerResetAt
        }
    }
    var pendingResetCompleted by remember { mutableStateOf(false) }

    WellnessScreen {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { PhysioBottomBar(selectedTab, onTabSelected) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = WellnessSpacing.Lg),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(WellnessSpacing.Md)
            ) {
                item {
                    WellnessHeader(
                        action = {
                            HeaderResetButton(onClick = { pendingResetCompleted = true })
                        }
                    )
                }

                item {
                    SettingsSection(title = "Session guidance") {
                        SettingsToggleRow(
                            iconRes = AppIcons.audioLines,
                            label = "Voice cues",
                            checked = appSettings.voiceCuesEnabled,
                            onCheckedChange = onVoiceCuesChange
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Vibration,
                            label = "Vibration feedback",
                            checked = appSettings.vibrationFeedbackEnabled,
                            onCheckedChange = onVibrationFeedbackChange
                        )
                        SettingsToggleRow(
                            iconRes = AppIcons.count,
                            label = "Speak countdown by default",
                            checked = appSettings.speakCountdownDefault,
                            onCheckedChange = onSpeakCountdownDefaultChange
                        )
                        SettingsToggleRow(
                            iconRes = AppIcons.audioLines,
                            label = "Motivation",
                            checked = appSettings.motivationEnabled,
                            onCheckedChange = onMotivationChange
                        )
                        if (appSettings.motivationEnabled) {
                            MotivationVoicePicker(
                                selected = appSettings.motivationVoice,
                                onSelected = onMotivationVoiceChange
                            )
                        }
                    }
                }

                item {
                    SettingsSection(title = "Comfort") {
                        SettingsToggleRow(
                            icon = Icons.Default.ScreenLockPortrait,
                            label = "Keep screen awake",
                            checked = appSettings.keepScreenAwake,
                            onCheckedChange = onKeepScreenAwakeChange
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Visibility,
                            label = "Larger timer text",
                            checked = appSettings.largerTimerText,
                            onCheckedChange = onLargerTimerTextChange
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Info,
                            label = "Reduced motion",
                            checked = appSettings.reducedMotion,
                            onCheckedChange = onReducedMotionChange
                        )
                    }
                }

                item {
                    SettingsSection(
                        title = "About",
                        subtitle = "PhysioTimer helps you run guided rehab routines with calm pacing."
                    ) {
                        Text(
                            text = "These preferences apply across sessions. Per-routine voice and countdown options can still be customized while editing a routine.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (pendingResetCompleted) {
        AlertDialog(
            onDismissRequest = { pendingResetCompleted = false },
            containerColor = WellnessSurfaces.Card,
            title = { Text(if (hasCompletedMarkers) "Reset completed marks?" else "No completed marks") },
            text = {
                Text(
                    if (hasCompletedMarkers) {
                        "This clears only card highlights. Session history in Progress stays unchanged."
                    } else {
                        "Complete a routine to see completed highlights on cards."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (hasCompletedMarkers) {
                            onResetCompletionMarkers()
                        }
                        pendingResetCompleted = false
                    }
                ) {
                    Text(if (hasCompletedMarkers) "Reset" else "OK")
                }
            },
            dismissButton = if (hasCompletedMarkers) {
                {
                    TextButton(onClick = { pendingResetCompleted = false }) {
                        Text("Cancel")
                    }
                }
            } else {
                null
            }
        )
    }
}

@Composable
internal fun PhysioBottomBar(selectedTab: AppScreen, onTabSelected: (AppScreen) -> Unit) {
    NavigationBar(
        containerColor = WellnessSurfaces.Card,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        )
    ) {
        BottomItem(
            selected = selectedTab == AppScreen.Home,
            icon = Icons.Default.Home,
            label = "Routines",
            onClick = { onTabSelected(AppScreen.Home) }
        )
        BottomItem(
            selected = selectedTab == AppScreen.Progress,
            iconRes = AppIcons.history,
            label = "Progress",
            onClick = { onTabSelected(AppScreen.Progress) }
        )
        BottomItem(
            selected = selectedTab == AppScreen.Settings,
            iconRes = AppIcons.settings,
            label = "Settings",
            onClick = { onTabSelected(AppScreen.Settings) }
        )
    }
}

@Composable
private fun CreateRoutineFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Create routine") }
    )
}

@Composable
private fun WellnessHeader(
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PhysioTimer",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        if (action != null) {
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}

@Composable
private fun HeaderResetButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            painter = painterResource(AppIcons.history),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Reset",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun WellnessSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(WellnessSurfaces.Card)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(AppIcons.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun EmptyPanel(title: String, message: String) {
    WellnessCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(AppIcons.play),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(WellnessColors.Beige200),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveRoutinePanel(
    exercises: List<Exercise>,
    expandedExerciseKey: String?,
    onExpandedExerciseKeyChange: (String?) -> Unit,
    onStart: (Exercise) -> Unit,
    onEdit: (Exercise) -> Unit,
    onDelete: (Exercise) -> Unit
) {
    WellnessCard(containerColor = WellnessSurfaces.Card) {
        exercises.forEachIndexed { index, exercise ->
            val key = exerciseCompletionKey(exercise)
            ActiveRoutineRow(
                exercise = exercise,
                isExpanded = expandedExerciseKey == key,
                onToggleExpanded = {
                    onExpandedExerciseKeyChange(
                        if (expandedExerciseKey == key) null else key
                    )
                },
                onStart = { onStart(exercise) },
                onEdit = { onEdit(exercise) },
                onDelete = { onDelete(exercise) }
            )
            if (index != exercises.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ActiveRoutineRow(
    exercise: Exercise,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onToggleExpanded)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseGlyph(imageUri = exercise.imageUri)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TinyStatusDot()
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = exercise.name.ifBlank { "Untitled routine" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse routine" else "Expand routine",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onStart,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Start")
            }
        }

        if (isExpanded) {
            Text(
                text = formatExerciseMeta(exercise),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            InlineStatRow(exercise = exercise)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.tertiary) }
            }
        }
    }
}

@Composable
private fun CompletedRoutineRow(
    exercise: Exercise,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleExpand: () -> Unit,
    isExpanded: Boolean
) {
    WellnessCard(
        modifier = Modifier.animateContentSize(),
        containerColor = WellnessColors.Sage50,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onToggleExpand),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(isCompleted = true)
            Spacer(Modifier.width(10.dp))
            Text(
                text = exercise.name.ifBlank { "Untitled routine" },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Completed routine",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onStart,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Start again")
            }
        }

        if (isExpanded) {
            Text(
                text = formatExerciseMeta(exercise),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            InlineStatRow(exercise = exercise)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.tertiary) }
            }
        }
    }
}

@Composable
private fun TinyStatusDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.75f))
    )
}

@Composable
private fun StatusDot(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(
                if (isCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun ExerciseGlyph(
    imageUri: String?
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = loadBoundedImageBitmap(context, imageUri, maxSizePx = 128)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WellnessColors.Sage100),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "Routine image",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(AppIcons.timer),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun InlineStatRow(exercise: Exercise) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InlineStatChip(label = "Steps", value = exercise.steps.size.toString(), modifier = Modifier.weight(1f))
        InlineStatChip(label = "Reps", value = exercise.reps.toString(), modifier = Modifier.weight(1f))
        InlineStatChip(label = "Time", value = formatClock(exercise.totalSeconds), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun InlineStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WellnessSurfaces.CardMuted)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun completedRoutineKeys(
    sessions: List<SessionRecord>,
    resetAtMillis: Long
): Set<String> {
    return sessions
        .asSequence()
        .filter { it.completed }
        .filter {
            val completedAt = if (it.endedAt > 0L) it.endedAt else it.startedAt
            completedAt > resetAtMillis
        }
        .map { completionKey(it.exerciseId, it.exerciseName) }
        .toSet()
}

private fun exerciseCompletionKey(exercise: Exercise): String {
    return completionKey(exercise.id.takeIf { it != 0L }, exercise.name)
}

private fun completionKey(exerciseId: Long?, exerciseName: String): String {
    return if (exerciseId != null) {
        "id:$exerciseId"
    } else {
        "name:${exerciseName.trim().lowercase(Locale.US)}"
    }
}

@Composable
private fun StreakGraphCard(streakData: StreakData) {
    WellnessCard(
        containerColor = WellnessSurfaces.Card,
        shape = RoundedCornerShape(30.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(WellnessColors.Sage100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(AppIcons.timer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "10 min/day keeps your streak alive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${streakData.currentStreak}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (streakData.currentStreak > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (streakData.currentStreak == 1) "day" else "days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(WellnessSpacing.Xs)) {
            WellnessChip(text = "Best ${streakData.longestStreak}d")
            val todayQualifies = streakData.last20Days[19].qualifies
            val todayText = when {
                streakData.todayMinutes == 0 -> "Today 0 / 10 min"
                todayQualifies -> "Today ${streakData.todayMinutes} min ✓"
                else -> "Today ${streakData.todayMinutes} / 10 min"
            }
            WellnessChip(
                text = todayText,
                containerColor = if (todayQualifies) MaterialTheme.colorScheme.primaryContainer
                                 else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (todayQualifies) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        StreakContributionGrid(
            days = streakData.last20Days,
            firstDayOfWeek = streakData.firstDayOfWeek
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WellnessSpacing.Xxs)
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            listOf(
                WellnessColors.Sage100,
                WellnessColors.Sage200,
                WellnessColors.Sage300,
                WellnessColors.Sage600
            ).forEach { color ->
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakContributionGrid(
    days: List<StreakDayData>,
    firstDayOfWeek: Int
) {
    val cellSizeDp = 11.dp
    val gapDp = 3.dp
    val labelWidthDp = 12.dp
    val rowLabels = listOf("M", "", "W", "", "F", "", "S")

    val totalCells = firstDayOfWeek + days.size
    val numCols = (totalCells + 6) / 7

    val density = LocalDensity.current
    val cellPx = with(density) { cellSizeDp.toPx() }
    val gapPx = with(density) { gapDp.toPx() }
    val stepPx = cellPx + gapPx

    val gridWidth = with(density) { (numCols * stepPx - gapPx).toDp() }
    val gridHeight = with(density) { (7 * stepPx - gapPx).toDp() }

    val cornerRadiusPx = cellPx * 0.3f
    val emptyOutColor = WellnessColors.Sage100.copy(alpha = 0.4f)

    fun colorForSeconds(s: Int): Color = when {
        s <= 0 -> WellnessColors.Sage100
        s < 300 -> WellnessColors.Sage200
        s < STREAK_THRESHOLD_SECONDS -> WellnessColors.Sage300
        else -> WellnessColors.Sage600
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier
                .width(labelWidthDp)
                .height(gridHeight),
            verticalArrangement = Arrangement.spacedBy(gapDp)
        ) {
            rowLabels.forEach { label ->
                Box(
                    modifier = Modifier
                        .width(labelWidthDp)
                        .height(cellSizeDp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Canvas(modifier = Modifier.size(gridWidth, gridHeight)) {
            for (col in 0 until numCols) {
                for (row in 0 until 7) {
                    val dayIndex = col * 7 + row - firstDayOfWeek
                    val color = if (dayIndex in days.indices) {
                        colorForSeconds(days[dayIndex].totalSeconds)
                    } else {
                        emptyOutColor
                    }
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(col * stepPx, row * stepPx),
                        size = Size(cellPx, cellPx),
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            }
        }
    }
}

internal data class StreakDayData(
    val totalSeconds: Int,
    val qualifies: Boolean
)

internal data class StreakData(
    val last20Days: List<StreakDayData>,
    val currentStreak: Int,
    val longestStreak: Int,
    val todayMinutes: Int,
    val firstDayOfWeek: Int
)

internal data class ProgressBucket(
    val sessionCount: Int,
    val routineCount: Int,
    val totalSeconds: Int
)

internal data class ProgressSummary(
    val today: ProgressBucket,
    val last7Days: ProgressBucket,
    val allTime: ProgressBucket
)

internal data class ProgressReport(
    val totalSessions: Int,
    val completedSessions: Int,
    val completionRatePercent: Int,
    val totalSeconds: Int,
    val averageSeconds: Int,
    val longestSeconds: Int
)

internal data class DailyActivity(
    val dayLabel: String,
    val totalSeconds: Int,
    val sessionCount: Int
)

internal fun computeProgressSummary(
    sessions: List<SessionRecord>,
    nowMillis: Long = System.currentTimeMillis()
): ProgressSummary {
    val started = sessions.filter { it.startedAt > 0L }
    val startOfToday = calendarStartOfDay(nowMillis)
    val startOfTomorrow = startOfToday + DAY_MILLIS
    val startOfLast7Days = startOfToday - (6L * DAY_MILLIS)

    fun bucket(predicate: (SessionRecord) -> Boolean): ProgressBucket {
        val items = started.filter(predicate)
        val distinctRoutines = items.map {
            val normalizedName = it.exerciseName.trim().lowercase()
            if (it.exerciseId != null) {
                "${it.exerciseId}:$normalizedName"
            } else {
                "name:$normalizedName"
            }
        }.distinct().size
        return ProgressBucket(
            sessionCount = items.size,
            routineCount = distinctRoutines,
            totalSeconds = items.sumOf { it.elapsedSeconds }
        )
    }

    return ProgressSummary(
        today = bucket { it.startedAt in startOfToday until startOfTomorrow },
        last7Days = bucket { it.startedAt in startOfLast7Days until startOfTomorrow },
        allTime = bucket { true }
    )
}

internal fun computeProgressReport(sessions: List<SessionRecord>): ProgressReport {
    val usable = sessions.filter { it.startedAt > 0L }
    val totalSessions = usable.size
    val completedSessions = usable.count { it.completed }
    val totalSeconds = usable.sumOf { it.elapsedSeconds }
    val completionRatePercent = if (totalSessions == 0) {
        0
    } else {
        ((completedSessions * 100f) / totalSessions).toInt()
    }

    return ProgressReport(
        totalSessions = totalSessions,
        completedSessions = completedSessions,
        completionRatePercent = completionRatePercent,
        totalSeconds = totalSeconds,
        averageSeconds = if (totalSessions == 0) 0 else totalSeconds / totalSessions,
        longestSeconds = usable.maxOfOrNull { it.elapsedSeconds } ?: 0
    )
}

internal fun computeSevenDayActivity(
    sessions: List<SessionRecord>,
    nowMillis: Long = System.currentTimeMillis()
): List<DailyActivity> {
    val usable = sessions.filter { it.startedAt > 0L }
    val startOfToday = calendarStartOfDay(nowMillis)
    return (6 downTo 0).map { dayOffset ->
        val dayStart = startOfToday - (dayOffset * DAY_MILLIS)
        val dayEnd = dayStart + DAY_MILLIS
        val dayItems = usable.filter { it.startedAt in dayStart until dayEnd }
        DailyActivity(
            dayLabel = dayOfWeekLabel(dayStart),
            totalSeconds = dayItems.sumOf { it.elapsedSeconds },
            sessionCount = dayItems.size
        )
    }
}

internal fun computeStreakData(
    sessions: List<SessionRecord>,
    nowMillis: Long = System.currentTimeMillis()
): StreakData {
    val startOfToday = calendarStartOfDay(nowMillis)
    val usable = sessions.filter { it.startedAt > 0L }

    val last20Days = (19 downTo 0).map { offset ->
        val dayStart = startOfToday - offset * DAY_MILLIS
        val totalSec = usable
            .filter { it.startedAt >= dayStart && it.startedAt < dayStart + DAY_MILLIS }
            .sumOf { it.elapsedSeconds }
        StreakDayData(totalSeconds = totalSec, qualifies = totalSec >= STREAK_THRESHOLD_SECONDS)
    }

    val startIdx = if (last20Days[19].qualifies) 19 else 18
    var currentStreak = 0
    for (i in startIdx downTo 0) {
        if (last20Days[i].qualifies) currentStreak++ else break
    }

    val qualifyingDayStarts = usable
        .groupBy { calendarStartOfDay(it.startedAt) }
        .filter { (_, s) -> s.sumOf { it.elapsedSeconds } >= STREAK_THRESHOLD_SECONDS }
        .keys
        .sorted()
    var longestStreak = 0
    var run = 0
    for (i in qualifyingDayStarts.indices) {
        run = if (i == 0 || qualifyingDayStarts[i] - qualifyingDayStarts[i - 1] == DAY_MILLIS) run + 1 else 1
        if (run > longestStreak) longestStreak = run
    }
    longestStreak = maxOf(longestStreak, currentStreak)

    val firstDayMillis = startOfToday - 19L * DAY_MILLIS
    val firstDow = (Calendar.getInstance().apply { timeInMillis = firstDayMillis }
        .get(Calendar.DAY_OF_WEEK) + 5) % 7

    return StreakData(
        last20Days = last20Days,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        todayMinutes = last20Days[19].totalSeconds / 60,
        firstDayOfWeek = firstDow
    )
}

private fun calendarStartOfDay(timeMillis: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun dayOfWeekLabel(timeMillis: Long): String {
    val day = Calendar.getInstance().apply { timeInMillis = timeMillis }.get(Calendar.DAY_OF_WEEK)
    return when (day) {
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        Calendar.SUNDAY -> "Sun"
        else -> "-"
    }
}

private fun formatSessionStamp(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Unknown"
    val now = System.currentTimeMillis()
    val startOfToday = calendarStartOfDay(now)
    val startOfYesterday = startOfToday - DAY_MILLIS
    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    return when {
        timeMillis >= startOfToday -> "Today ${timeFormat.format(Date(timeMillis))}"
        timeMillis >= startOfYesterday -> "Yesterday ${timeFormat.format(Date(timeMillis))}"
        else -> {
            val fullFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.US)
            fullFormat.format(Date(timeMillis))
        }
    }
}

private fun formatProgressDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

@Composable
private fun ProgressOverview(summary: ProgressSummary) {
    WellnessCard(containerColor = WellnessSurfaces.LayerWarm) {
        Text(
            text = "Time spent",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Based on recorded session time in each period.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ProgressStatRow(
            label = "Today",
            bucket = summary.today
        )
        ProgressStatRow(
            label = "Last 7 days",
            bucket = summary.last7Days
        )
        ProgressStatRow(
            label = "All time",
            bucket = summary.allTime
        )
    }
}

@Composable
private fun ProgressStatRow(
    label: String,
    bucket: ProgressBucket
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WellnessSurfaces.Card)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${bucket.sessionCount} sessions - ${bucket.routineCount} routines",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatProgressDuration(bucket.totalSeconds),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProgressHighlights(report: ProgressReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReportMetricCard(
            title = "Done",
            value = "${report.completionRatePercent}%",
            subtitle = "${report.completedSessions}/${report.totalSessions} sessions",
            modifier = Modifier.weight(1f)
        )
        ReportMetricCard(
            title = "Average",
            value = formatProgressDuration(report.averageSeconds),
            subtitle = "per session",
            modifier = Modifier.weight(1f)
        )
        ReportMetricCard(
            title = "Longest",
            value = formatProgressDuration(report.longestSeconds),
            subtitle = "best run",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    WellnessCard(modifier = modifier, containerColor = WellnessSurfaces.CardMuted) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompletionRingReport(report: ProgressReport) {
    WellnessCard {
        Text(
            text = "Completion quality",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Completed sessions versus stopped sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompletionRing(
                completionRatePercent = report.completionRatePercent,
                modifier = Modifier.size(108.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Completed: ${report.completedSessions}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Stopped: ${report.totalSessions - report.completedSessions}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total time: ${formatProgressDuration(report.totalSeconds)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompletionRing(
    completionRatePercent: Int,
    modifier: Modifier = Modifier
) {
    val progress = completionRatePercent.coerceIn(0, 100) / 100f
    val activeColor = MaterialTheme.colorScheme.primary
    val trackColor = WellnessColors.Sage100
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${completionRatePercent}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "done",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SevenDayActivityReport(weekly: List<DailyActivity>) {
    val maxSeconds = (weekly.maxOfOrNull { it.totalSeconds } ?: 0).coerceAtLeast(1)
    WellnessCard(containerColor = WellnessSurfaces.LayerWarm) {
        Text(
            text = "Last 7 days activity",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Daily time spent in sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            weekly.forEach { day ->
                val ratio = day.totalSeconds.toFloat() / maxSeconds.toFloat()
                val barHeight = if (day.totalSeconds == 0) 8.dp else (18.dp + (92.dp * ratio))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (day.totalSeconds == 0) "-" else formatProgressDuration(day.totalSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .height(112.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.64f)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                                .background(
                                    if (day.totalSeconds > 0) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                        )
                    }
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${day.sessionCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSessionReportCard(sessions: List<SessionRecord>) {
    WellnessCard {
        Text(
            text = "Recent sessions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Read-only log of latest activity.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        sessions.forEachIndexed { index, session ->
            if (index > 0) Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WellnessSurfaces.CardMuted)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.exerciseName.ifBlank { "Routine" },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatSessionStamp(session.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatProgressDuration(session.elapsedSeconds),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (session.completed) "completed" else "stopped",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (session.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun MotivationVoicePicker(
    selected: MotivationVoiceOption,
    onSelected: (MotivationVoiceOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Motivation voice",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MotivationVoiceOption.values().forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    label = { Text(option.label) }
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    @androidx.annotation.DrawableRes iconRes: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WellnessSurfaces.Card)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            when {
                icon != null -> Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                iconRes != null -> Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
