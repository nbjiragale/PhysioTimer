package com.niranjan.physiotimer.ui

import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.niranjan.physiotimer.data.AppSettings
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.feedback.FeedbackController
import com.niranjan.physiotimer.feedback.VoicePromptCatalog
import com.niranjan.physiotimer.timer.SessionTimer
import com.niranjan.physiotimer.timer.TimerCue
import com.niranjan.physiotimer.timer.TimerSnapshot
import com.niranjan.physiotimer.timer.TimerStatus
import kotlinx.coroutines.delay

@Composable
internal fun ActiveTimerScreen(
    exercise: Exercise,
    appSettings: AppSettings,
    feedbackController: FeedbackController,
    onStop: (Int) -> Unit,
    onComplete: (Int) -> Unit
) {
    val effectiveExercise = remember(exercise, appSettings) {
        exercise.copy(
            voiceEnabled = exercise.voiceEnabled && appSettings.voiceCuesEnabled,
            vibrationEnabled = exercise.vibrationEnabled && appSettings.vibrationFeedbackEnabled
        )
    }
    val timer = remember(effectiveExercise) { SessionTimer(effectiveExercise) }
    var update by remember(timer) { mutableStateOf(timer.start(timerNowMs())) }
    var completed by remember(timer) { mutableStateOf(false) }
    var autoPausedForMotivation by remember(timer) { mutableStateOf(false) }
    var deferredPostMotivationCues by remember(timer) { mutableStateOf<List<TimerCue>>(emptyList()) }
    val motivationPlaying by feedbackController.motivationPlayback.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = androidx.compose.ui.platform.LocalContext.current.findActivity()

    fun dispatchCues(cues: List<TimerCue>) {
        val sanitization = sanitizeCuesForMotivation(cues, appSettings)
        val hasMotivationCue = sanitization.hasMotivationCue
        if (hasMotivationCue) {
            deferredPostMotivationCues = sanitization.deferredCues
        }
        if (hasMotivationCue && !autoPausedForMotivation && update.snapshot.status == TimerStatus.Running) {
            update = timer.pause(timerNowMs(), announce = false)
            autoPausedForMotivation = true
        }
        feedbackController.handleCues(
            sanitization.cues,
            effectiveExercise.beepEnabled,
            effectiveExercise.vibrationEnabled
        )
    }

    DisposableEffect(activity, appSettings.keepScreenAwake) {
        if (appSettings.keepScreenAwake) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    DisposableEffect(lifecycleOwner, timer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) update = timer.pause(timerNowMs())
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(timer) {
        dispatchCues(update.cues)
        while (!completed) {
            delay(64L)
            val next = timer.tick(timerNowMs())
            update = next
            dispatchCues(next.cues)
            if (next.snapshot.status == TimerStatus.Complete) {
                completed = true
                delay(450L)
                onComplete((next.snapshot.totalElapsedMillis / 1_000L).toInt())
            }
        }
    }
    LaunchedEffect(motivationPlaying) {
        val nowMs = timerNowMs()
        if (motivationPlaying) {
            if (!autoPausedForMotivation && update.snapshot.status == TimerStatus.Running) {
                update = timer.pause(nowMs, announce = false)
                autoPausedForMotivation = true
            }
        } else if (autoPausedForMotivation) {
            if (update.snapshot.status == TimerStatus.Paused) {
                update = timer.resume(nowMs, announce = false)
                if (deferredPostMotivationCues.isNotEmpty()) {
                    feedbackController.handleCues(
                        deferredPostMotivationCues,
                        effectiveExercise.beepEnabled,
                        effectiveExercise.vibrationEnabled
                    )
                    deferredPostMotivationCues = emptyList()
                }
            }
            autoPausedForMotivation = false
        }
    }

    val snapshot = update.snapshot
    val isRunning = snapshot.status == TimerStatus.Running
    val progress = if (snapshot.stepDurationMillis == 0L) {
        0f
    } else {
        (1f - snapshot.remainingMillis.toFloat() / snapshot.stepDurationMillis.toFloat()).coerceIn(0f, 1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = if (appSettings.reducedMotion) 0 else 140),
        label = "timerProgress"
    )
    val rawStepAccent = composeColor(snapshot.stepColorArgb)
    val animatedStepAccent by animateColorAsState(
        targetValue = rawStepAccent,
        animationSpec = tween(durationMillis = if (appSettings.reducedMotion) 0 else 220),
        label = "stepAccent"
    )
    val stepAccent = if (appSettings.reducedMotion) rawStepAccent else animatedStepAccent
    val stopAndExit = {
        val now = timerNowMs()
        val finalUpdate = timer.stop(now)
        update = finalUpdate
        feedbackController.stopAllAudio()
        onStop((finalUpdate.snapshot.totalElapsedMillis / 1_000L).toInt())
    }

    WellnessScreen(showOrganicBackground = false) {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = WellnessSpacing.Lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActiveTopBar(
                    exerciseName = effectiveExercise.name,
                    onBack = stopAndExit
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val compactLayout = maxHeight < 520.dp
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            if (compactLayout) WellnessSpacing.Xs else WellnessSpacing.Sm,
                            Alignment.CenterVertically
                        )
                    ) {
                        RepCounter(
                            snapshot = snapshot,
                            autoPausedForMotivation = autoPausedForMotivation,
                            compactLayout = compactLayout
                        )

                        TimerSanctuary(
                            snapshot = snapshot,
                            progress = animatedProgress,
                            accent = stepAccent,
                            isRunning = isRunning,
                            largerTimerText = appSettings.largerTimerText,
                            reducedMotion = appSettings.reducedMotion,
                            compactLayout = compactLayout
                        )
                    }
                }

                SessionControls(
                    isRunning = isRunning,
                    isPreparing = snapshot.isPreparing,
                    onPlayPause = {
                        if (autoPausedForMotivation) return@SessionControls
                        val next = if (snapshot.isPreparing) {
                            timer.skipStep(timerNowMs())
                        } else if (isRunning) {
                            timer.pause(timerNowMs())
                        } else {
                            timer.resume(timerNowMs())
                        }
                        update = next
                        dispatchCues(next.cues)
                    }
                )

                TextButton(
                    onClick = stopAndExit,
                    modifier = Modifier.padding(top = WellnessSpacing.Xs, bottom = WellnessSpacing.Sm)
                ) {
                    Icon(
                        painter = painterResource(AppIcons.stop),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Stop session", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

private fun applyMotivationSettings(cues: List<TimerCue>, appSettings: AppSettings): List<TimerCue> {
    return cues.mapNotNull { cue ->
        if (cue !is TimerCue.Speak) {
            return@mapNotNull cue
        }
        if (!VoicePromptCatalog.isConfigurableMotivationCue(cue.text)) {
            return@mapNotNull cue
        }
        if (!appSettings.motivationEnabled) {
            return@mapNotNull null
        }
        val replacement = if (VoicePromptCatalog.usesCompletionMotivationVoice(cue.text)) {
            appSettings.motivationVoice.completionPrompt
        } else {
            appSettings.motivationVoice.runningPrompt
        }
        TimerCue.Speak(replacement)
    }
}

internal data class MotivationCueSanitization(
    val cues: List<TimerCue>,
    val hasMotivationCue: Boolean,
    val deferredCues: List<TimerCue>
)

internal fun sanitizeCuesForMotivation(
    cues: List<TimerCue>,
    appSettings: AppSettings
): MotivationCueSanitization {
    val adjusted = applyMotivationSettings(cues, appSettings)
    val hasMotivationCue = adjusted.any { cue ->
        cue is TimerCue.Speak && (
            VoicePromptCatalog.shouldPauseCounterDuringCue(cue.text)
            )
    }
    val filtered = if (hasMotivationCue) {
        adjusted.filterNot { cue ->
            cue is TimerCue.PlayExactCounter || (cue is TimerCue.Speak && cue.text.toIntOrNull() != null)
        }
    } else {
        adjusted
    }
    val deferred = if (hasMotivationCue) {
        adjusted.filter { cue ->
            cue is TimerCue.PlayExactCounter || (cue is TimerCue.Speak && cue.text.toIntOrNull() != null)
        }
    } else {
        emptyList()
    }
    return MotivationCueSanitization(
        cues = filtered,
        hasMotivationCue = hasMotivationCue,
        deferredCues = deferred
    )
}

@Composable
private fun ActiveTopBar(
    exerciseName: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = WellnessSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoftIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClick = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exerciseName.ifBlank { "Routine" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.size(46.dp))
    }
}

@Composable
private fun RepCounter(
    snapshot: TimerSnapshot,
    autoPausedForMotivation: Boolean,
    compactLayout: Boolean
) {
    val statusText = when {
        autoPausedForMotivation -> "Motivation cue playing - timer will resume automatically"
        snapshot.isPreparing -> "Preparing to begin"
        snapshot.status == TimerStatus.Paused -> "Paused - resume when ready"
        else -> null
    }
    val repsStyle = MaterialTheme.typography.displayMedium.copy(
        fontSize = if (compactLayout) 56.sp else 64.sp,
        lineHeight = if (compactLayout) 58.sp else 66.sp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WellnessSpacing.Xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WellnessSpacing.Xxs)
    ) {
        Text(
            text = "Reps",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${snapshot.currentRep}/${snapshot.totalReps}",
            style = repsStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (statusText != null) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TimerSanctuary(
    snapshot: TimerSnapshot,
    progress: Float,
    accent: Color,
    isRunning: Boolean,
    largerTimerText: Boolean,
    reducedMotion: Boolean,
    compactLayout: Boolean
) {
    val arcSize = if (compactLayout) 186.dp else 218.dp
    val secondsStyle = if (largerTimerText) {
        MaterialTheme.typography.displayLarge.copy(
            fontSize = if (compactLayout) 72.sp else 78.sp,
            lineHeight = if (compactLayout) 76.sp else 82.sp
        )
    } else {
        MaterialTheme.typography.displayLarge.copy(
            fontSize = if (compactLayout) 58.sp else 64.sp,
            lineHeight = if (compactLayout) 62.sp else 68.sp
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = WellnessSurfaces.Card,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WellnessSpacing.Lg, vertical = WellnessSpacing.Md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WellnessSpacing.Sm)
        ) {
            WellnessChip(
                text = if (snapshot.isPreparing) "Starting soon" else snapshot.stepName,
                modifier = Modifier.fillMaxWidth(),
                containerColor = accent.copy(alpha = 0.14f),
                contentColor = accent,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier.size(arcSize),
                contentAlignment = Alignment.Center
            ) {
                GentleProgressArc(
                    progress = progress,
                    activeColor = if (snapshot.status == TimerStatus.Paused) WellnessColors.Lavender400 else accent,
                    secondaryColor = WellnessColors.Lavender600,
                    trackColor = WellnessColors.Sage100,
                    isRunning = isRunning && !reducedMotion,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (reducedMotion) {
                        Text(
                            text = snapshot.remainingSeconds.toString().padStart(2, '0'),
                            style = secondsStyle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        AnimatedContent(targetState = snapshot.remainingSeconds, label = "seconds") { value ->
                            Text(
                                text = value.toString().padStart(2, '0'),
                                style = secondsStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = "seconds",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = guidanceCopy(snapshot, isRunning),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun guidanceCopy(snapshot: TimerSnapshot, isRunning: Boolean): String {
    return when {
        snapshot.isPreparing -> "Settle in. Your first movement starts soon."
        snapshot.status == TimerStatus.Paused -> "Paused. Resume when you feel ready."
        snapshot.stepIndex == snapshot.totalSteps - 1 && snapshot.currentRep == snapshot.totalReps -> "Almost there. Stay steady."
        snapshot.stepName.contains("rest", ignoreCase = true) || snapshot.stepName.contains("relax", ignoreCase = true) -> {
            "Release tension and reset your posture."
        }
        isRunning -> "Move slowly and keep your breathing steady."
        else -> "Ready when you are."
    }
}

@Composable
private fun SessionControls(
    isRunning: Boolean,
    isPreparing: Boolean,
    onPlayPause: () -> Unit
) {
    val playContainer = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val playIconRes = if (isPreparing || !isRunning) AppIcons.play else AppIcons.pause

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(playContainer)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(playIconRes),
                contentDescription = if (isRunning) "Pause" else "Resume",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
