package com.niranjan.physiotimer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.niranjan.physiotimer.data.AppSettingsRepository
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseRepository
import com.niranjan.physiotimer.data.defaultSteps
import com.niranjan.physiotimer.feedback.FeedbackController
import kotlinx.coroutines.launch

@Composable
fun PhysioRepTimerApp(
    repository: ExerciseRepository,
    appSettingsRepository: AppSettingsRepository,
    feedbackController: FeedbackController
) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var homeExpandedExerciseKey by rememberSaveable { mutableStateOf<String?>(null) }
    val appSettings by appSettingsRepository.settings.collectAsState()
    val scope = rememberCoroutineScope()

    fun launchTimer(exercise: Exercise) {
        scope.launch {
            val sessionId = runCatching { repository.startSession(exercise) }.getOrDefault(0L)
            screen = AppScreen.Timer(exercise, sessionId)
        }
    }

    when (val current = screen) {
        AppScreen.Home -> HomeScreen(
            repository = repository,
            selectedTab = AppScreen.Home,
            onTabSelected = { screen = it },
            completionMarkerResetAt = appSettings.completionMarkerResetAt,
            onResetCompletionMarkers = appSettingsRepository::resetCompletionMarkers,
            expandedExerciseKey = homeExpandedExerciseKey,
            onExpandedExerciseKeyChange = { homeExpandedExerciseKey = it },
            onCreate = {
                screen = AppScreen.Editor(
                    Exercise(
                        voiceEnabled = appSettings.voiceCuesEnabled,
                        vibrationEnabled = appSettings.vibrationFeedbackEnabled,
                        startCountdownCountAloudEnabled = appSettings.speakCountdownDefault,
                        steps = defaultSteps().map { step ->
                            step.copy(
                                countAloudEnabled = appSettings.speakCountdownDefault
                            )
                        }
                    )
                )
            },
            onEdit = { screen = AppScreen.Editor(it) },
            onStart = { launchTimer(it) }
        )

        AppScreen.Progress -> ProgressScreen(
            selectedTab = AppScreen.Progress,
            onTabSelected = { screen = it },
            repository = repository,
            completionMarkerResetAt = appSettings.completionMarkerResetAt,
            onResetCompletionMarkers = appSettingsRepository::resetCompletionMarkers,
            onStart = { launchTimer(it) },
            onEdit = { screen = AppScreen.Editor(it) }
        )

        AppScreen.Settings -> SettingsScreen(
            selectedTab = AppScreen.Settings,
            onTabSelected = { screen = it },
            repository = repository,
            appSettings = appSettings,
            onResetCompletionMarkers = appSettingsRepository::resetCompletionMarkers,
            onVoiceCuesChange = appSettingsRepository::setVoiceCuesEnabled,
            onVibrationFeedbackChange = appSettingsRepository::setVibrationFeedbackEnabled,
            onSpeakCountdownDefaultChange = appSettingsRepository::setSpeakCountdownDefault,
            onMotivationChange = appSettingsRepository::setMotivationEnabled,
            onMotivationVoiceChange = appSettingsRepository::setMotivationVoice,
            onKeepScreenAwakeChange = appSettingsRepository::setKeepScreenAwake,
            onLargerTimerTextChange = appSettingsRepository::setLargerTimerText,
            onReducedMotionChange = appSettingsRepository::setReducedMotion
        )

        is AppScreen.Editor -> EditExerciseScreen(
            initial = current.initial,
            repository = repository,
            onDone = { screen = AppScreen.Home },
            onCancel = { screen = AppScreen.Home }
        )

        is AppScreen.Timer -> ActiveTimerScreen(
            exercise = current.exercise,
            appSettings = appSettings,
            feedbackController = feedbackController,
            onStop = { elapsedSeconds ->
                scope.launch {
                    repository.finishSession(
                        sessionId = current.sessionId,
                        elapsedSeconds = elapsedSeconds,
                        completed = false
                    )
                }
                screen = AppScreen.Home
            },
            onComplete = { elapsedSeconds ->
                scope.launch {
                    repository.finishSession(
                        sessionId = current.sessionId,
                        elapsedSeconds = elapsedSeconds,
                        completed = true
                    )
                }
                screen = AppScreen.Complete(current.exercise)
            }
        )

        is AppScreen.Complete -> CompletionScreen(
            exercise = current.exercise,
            onDone = { screen = AppScreen.Home },
            onRepeat = { launchTimer(current.exercise) }
        )
    }
}
