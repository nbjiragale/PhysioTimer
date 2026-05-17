package com.niranjan.physiotimer.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseRepository
import com.niranjan.physiotimer.data.ExerciseStep
import com.niranjan.physiotimer.data.validateExercise
import com.niranjan.physiotimer.feedback.VoicePromptCatalog
import kotlinx.coroutines.launch

@Composable
internal fun EditExerciseScreen(
    initial: Exercise,
    repository: ExerciseRepository,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var draft by remember(initial) { mutableStateOf(initial) }
    var errors by remember { mutableStateOf(emptyList<String>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            draft = draft.copy(imageUri = uri.toString())
        }
    }

    fun save() {
        val validation = validateExercise(draft)
        errors = validation
        if (validation.isEmpty()) {
            scope.launch {
                repository.saveExercise(draft)
                onDone()
            }
        }
    }

    WellnessScreen {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = WellnessSpacing.Lg)
            ) {
                EditTopBar(
                    isNew = initial.id == 0L,
                    onBack = onCancel,
                    onSave = ::save
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(WellnessSpacing.Md),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        WellnessCard(containerColor = WellnessSurfaces.LayerWarm) {
                            Text("Routine logo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            RoutineLogoPickerRow(
                                imageUri = draft.imageUri,
                                onPick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                                onClear = { draft = draft.copy(imageUri = null) }
                            )
                            Text("Routine name", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LargeTextField(
                                value = draft.name,
                                onValueChange = { draft = draft.copy(name = it) },
                                placeholder = "Shoulder mobility flow"
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WellnessChip(text = "${draft.steps.size} steps")
                                WellnessChip(text = "${draft.reps} reps")
                                WellnessChip(text = formatClock(draft.totalSeconds))
                            }
                        }
                    }

                    if (errors.isNotEmpty()) {
                        item { ErrorPanel(errors) }
                    }

                    item {
                        SectionHeader(
                            title = "Steps",
                            subtitle = "Build a gentle guided sequence"
                        )
                    }

                    itemsIndexed(draft.steps, key = { index, step -> if (step.id != 0L) step.id else index }) { index, step ->
                        EditorStepCard(
                            index = index,
                            step = step,
                            onStepChange = { updated ->
                                draft = draft.copy(steps = draft.steps.replaceAt(index, updated))
                            },
                            onDelete = {
                                draft = draft.copy(steps = draft.steps.filterIndexed { itemIndex, _ -> itemIndex != index })
                            }
                        )
                    }

                    item {
                        TonalWellnessButton(
                            text = "+ Add another step",
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Add,
                            onClick = {
                                val accent = StepAccentPalette[draft.steps.size % StepAccentPalette.size]
                                draft = draft.copy(
                                    steps = draft.steps + ExerciseStep(
                                        name = "Step ${draft.steps.size + 1}",
                                        colorArgb = accent.primary.toArgb().toLong() and 0xFFFFFFFFL
                                    )
                                )
                            }
                        )
                    }

                    item {
                        SectionHeader(
                            title = "Session rhythm",
                            subtitle = "Configure countdown and repetition pacing"
                        )
                    }

                    item {
                        WellnessCard {
                            StepperRow(
                                label = "Repetitions",
                                value = draft.reps,
                                min = 1,
                                onValueChange = { draft = draft.copy(reps = it.coerceAtLeast(1)) }
                            )
                            StepperRow(
                                label = "Start countdown",
                                value = draft.startDelaySeconds,
                                min = 0,
                                onValueChange = { draft = draft.copy(startDelaySeconds = it.coerceAtLeast(0)) }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Speak countdown",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                SmallSwitch(
                                    checked = draft.startCountdownCountAloudEnabled,
                                    color = MaterialTheme.colorScheme.primary,
                                    onCheckedChange = { draft = draft.copy(startCountdownCountAloudEnabled = it) }
                                )
                            }
                            StepperRow(
                                label = "Countdown interval",
                                value = draft.startCountdownIntervalSeconds,
                                min = 1,
                                onValueChange = { draft = draft.copy(startCountdownIntervalSeconds = it.coerceAtLeast(1)) }
                            )
                        }
                    }

                    item {
                        PrimaryWellnessButton(
                            text = "Save routine",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = WellnessSpacing.Xxl),
                            onClick = ::save
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineLogoPickerRow(
    imageUri: String?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoutineLogoPreview(
            imageUri = imageUri,
            modifier = Modifier.size(74.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (imageUri.isNullOrBlank()) "No image selected" else "Image selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "This appears in the routine square icon.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            TextButton(onClick = onPick) { Text(if (imageUri.isNullOrBlank()) "Upload" else "Change") }
            if (!imageUri.isNullOrBlank()) {
                TextButton(onClick = onClear) { Text("Remove") }
            }
        }
    }
}

@Composable
private fun RoutineLogoPreview(
    imageUri: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = loadBoundedImageBitmap(context, imageUri, maxSizePx = 192)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(WellnessColors.Sage100)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "Routine logo",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = androidx.compose.ui.res.painterResource(AppIcons.timer),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun EditTopBar(
    isNew: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = WellnessSpacing.Md, bottom = WellnessSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoftIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClick = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = if (isNew) "Create routine" else "Edit routine",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Build a gentle guided flow",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PrimaryWellnessButton(text = "Save", modifier = Modifier.widthIn(min = 92.dp), onClick = onSave)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun EditorStepCard(
    index: Int,
    step: ExerciseStep,
    onStepChange: (ExerciseStep) -> Unit,
    onDelete: () -> Unit
) {
    val accent = composeColor(step.colorArgb)
    val hasRecordedVoice = remember(step.name) {
        VoicePromptCatalog.hasRecordedVoiceForStep(step.name)
    }
    val voiceSuggestions = remember(step.name) {
        val current = step.name.trim()
        VoicePromptCatalog
            .suggestedStepNames(current)
            .filterNot { suggestion -> suggestion.equals(current, ignoreCase = true) }
    }

    WellnessCard(containerColor = accent.copy(alpha = 0.12f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(10.dp))
            CompactEditorTextField(
                value = step.name,
                onValueChange = { onStepChange(step.copy(name = it)) },
                placeholder = "Step name",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            CompactNumberField(
                value = step.durationSeconds,
                onValueChange = { onStepChange(step.copy(durationSeconds = it.coerceAtLeast(0))) },
                suffix = "s",
                iconRes = AppIcons.timer,
                modifier = Modifier.width(96.dp)
            )
            Spacer(Modifier.width(6.dp))
            SoftIconButton(Icons.Default.Delete, "Delete step", onClick = onDelete)
        }

        if (step.name.isNotBlank()) {
            StepVoiceAvailability(
                hasRecordedVoice = hasRecordedVoice
            )
        }

        if (voiceSuggestions.isNotEmpty()) {
            StepNameSuggestions(
                suggestions = voiceSuggestions,
                onSelect = { selected ->
                    onStepChange(step.copy(name = selected))
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleLabel(iconRes = AppIcons.audioLines, text = "Voice", tint = accent)
            Spacer(Modifier.width(8.dp))
            SmallSwitch(checked = step.voicePromptEnabled, color = accent) {
                onStepChange(step.copy(voicePromptEnabled = it))
            }
            Spacer(Modifier.weight(1f))
            ToggleLabel(iconRes = AppIcons.count, text = "Count", tint = accent)
            Spacer(Modifier.width(8.dp))
            SmallSwitch(checked = step.countAloudEnabled, color = accent) {
                onStepChange(step.copy(countAloudEnabled = it))
            }
        }

        if (step.countAloudEnabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Count every",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                CompactNumberField(
                    value = step.countIntervalSeconds,
                    onValueChange = { onStepChange(step.copy(countIntervalSeconds = it.coerceAtLeast(0))) },
                    suffix = "s",
                    iconRes = null,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}

@Composable
private fun StepVoiceAvailability(hasRecordedVoice: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WellnessSurfaces.Card.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val title = if (hasRecordedVoice) "Recorded voice found" else "TTS fallback"
        val detail = if (hasRecordedVoice) {
            "This step will use app voice audio."
        } else {
            "No matching clip yet, text-to-speech will be used."
        }
        Text(
            text = "$title - $detail",
            style = MaterialTheme.typography.labelMedium,
            color = if (hasRecordedVoice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepNameSuggestions(
    suggestions: List<String>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Suggested step names",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                FilterChip(
                    selected = false,
                    onClick = { onSelect(suggestion) },
                    label = { Text(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun ToggleLabel(
    @androidx.annotation.DrawableRes iconRes: Int,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(min = 46.dp)
        )
    }
}

@Composable
private fun LargeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WellnessSurfaces.Card)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun CompactEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.84f))
            .border(1.dp, Color.White.copy(alpha = 0.95f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth(),
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
