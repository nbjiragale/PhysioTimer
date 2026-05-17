package com.niranjan.physiotimer.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val voiceCuesEnabled: Boolean = true,
    val vibrationFeedbackEnabled: Boolean = true,
    val speakCountdownDefault: Boolean = true,
    val motivationEnabled: Boolean = true,
    val motivationVoice: MotivationVoiceOption = MotivationVoiceOption.KEEP_GOING,
    val keepScreenAwake: Boolean = true,
    val largerTimerText: Boolean = false,
    val reducedMotion: Boolean = false,
    val completionMarkerResetAt: Long = 0L
)

enum class MotivationVoiceOption(
    val storageKey: String,
    val label: String,
    val runningPrompt: String,
    val completionPrompt: String
) {
    KEEP_GOING(
        storageKey = "keep_going",
        label = "Keep going",
        runningPrompt = "Keep going",
        completionPrompt = "Well done"
    ),
    NICE_STEADY_PACE(
        storageKey = "nice_steady_pace",
        label = "Nice steady pace",
        runningPrompt = "Nice steady pace",
        completionPrompt = "Well done"
    ),
    BREATHE_AND_MOVE(
        storageKey = "breathe_and_move",
        label = "Breathe and move",
        runningPrompt = "Breathe and move",
        completionPrompt = "Well done"
    ),
    ALMOST_THERE(
        storageKey = "almost_there",
        label = "Almost there",
        runningPrompt = "Almost there",
        completionPrompt = "Well done"
    ),
    LIE_DOWN_OR_SIT(
        storageKey = "lie_down_or_sit",
        label = "Lie down or sit",
        runningPrompt = "Lie down or sit",
        completionPrompt = "Well done"
    );

    companion object {
        fun fromStorageKey(value: String?): MotivationVoiceOption {
            return values().firstOrNull { it.storageKey == value } ?: KEEP_GOING
        }
    }
}

class AppSettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setVoiceCuesEnabled(enabled: Boolean) {
        updateAndPersist { it.copy(voiceCuesEnabled = enabled) }
    }

    fun setVibrationFeedbackEnabled(enabled: Boolean) {
        updateAndPersist { it.copy(vibrationFeedbackEnabled = enabled) }
    }

    fun setSpeakCountdownDefault(enabled: Boolean) {
        updateAndPersist { it.copy(speakCountdownDefault = enabled) }
    }

    fun setMotivationEnabled(enabled: Boolean) {
        updateAndPersist { it.copy(motivationEnabled = enabled) }
    }

    fun setMotivationVoice(option: MotivationVoiceOption) {
        updateAndPersist { it.copy(motivationVoice = option) }
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        updateAndPersist { it.copy(keepScreenAwake = enabled) }
    }

    fun setLargerTimerText(enabled: Boolean) {
        updateAndPersist { it.copy(largerTimerText = enabled) }
    }

    fun setReducedMotion(enabled: Boolean) {
        updateAndPersist { it.copy(reducedMotion = enabled) }
    }

    fun resetCompletionMarkers() {
        updateAndPersist { it.copy(completionMarkerResetAt = System.currentTimeMillis()) }
    }

    private fun updateAndPersist(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
        _settings.value = next
        prefs.edit()
            .putBoolean(KEY_VOICE_CUES, next.voiceCuesEnabled)
            .putBoolean(KEY_VIBRATION_FEEDBACK, next.vibrationFeedbackEnabled)
            .putBoolean(KEY_SPEAK_COUNTDOWN_DEFAULT, next.speakCountdownDefault)
            .putBoolean(KEY_MOTIVATION_ENABLED, next.motivationEnabled)
            .putString(KEY_MOTIVATION_VOICE, next.motivationVoice.storageKey)
            .putBoolean(KEY_KEEP_SCREEN_AWAKE, next.keepScreenAwake)
            .putBoolean(KEY_LARGER_TIMER_TEXT, next.largerTimerText)
            .putBoolean(KEY_REDUCED_MOTION, next.reducedMotion)
            .putLong(KEY_COMPLETION_MARKER_RESET_AT, next.completionMarkerResetAt)
            .apply()
    }

    private fun readSettings(): AppSettings {
        return AppSettings(
            voiceCuesEnabled = prefs.getBoolean(KEY_VOICE_CUES, true),
            vibrationFeedbackEnabled = prefs.getBoolean(KEY_VIBRATION_FEEDBACK, true),
            speakCountdownDefault = prefs.getBoolean(KEY_SPEAK_COUNTDOWN_DEFAULT, true),
            motivationEnabled = prefs.getBoolean(KEY_MOTIVATION_ENABLED, true),
            motivationVoice = MotivationVoiceOption.fromStorageKey(
                prefs.getString(KEY_MOTIVATION_VOICE, MotivationVoiceOption.KEEP_GOING.storageKey)
            ),
            keepScreenAwake = prefs.getBoolean(KEY_KEEP_SCREEN_AWAKE, true),
            largerTimerText = prefs.getBoolean(KEY_LARGER_TIMER_TEXT, false),
            reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false),
            completionMarkerResetAt = prefs.getLong(KEY_COMPLETION_MARKER_RESET_AT, 0L)
        )
    }

    private companion object {
        const val PREFS_NAME = "physio_timer_app_settings"

        const val KEY_VOICE_CUES = "voice_cues_enabled"
        const val KEY_VIBRATION_FEEDBACK = "vibration_feedback_enabled"
        const val KEY_SPEAK_COUNTDOWN_DEFAULT = "speak_countdown_default"
        const val KEY_MOTIVATION_ENABLED = "motivation_enabled"
        const val KEY_MOTIVATION_VOICE = "motivation_voice"
        const val KEY_KEEP_SCREEN_AWAKE = "keep_screen_awake"
        const val KEY_LARGER_TIMER_TEXT = "larger_timer_text"
        const val KEY_REDUCED_MOTION = "reduced_motion"
        const val KEY_COMPLETION_MARKER_RESET_AT = "completion_marker_reset_at"
    }
}
