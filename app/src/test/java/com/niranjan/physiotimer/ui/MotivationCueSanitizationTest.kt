package com.niranjan.physiotimer.ui

import com.niranjan.physiotimer.data.AppSettings
import com.niranjan.physiotimer.data.MotivationVoiceOption
import com.niranjan.physiotimer.timer.TimerCue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotivationCueSanitizationTest {
    @Test
    fun removesNumericAndExactCounterWhenMotivationIsPresent() {
        val appSettings = AppSettings(
            motivationEnabled = true,
            motivationVoice = MotivationVoiceOption.ALMOST_THERE
        )
        val cues = listOf(
            TimerCue.Speak("Keep going"),
            TimerCue.Speak("5"),
            TimerCue.PlayExactCounter(10),
            TimerCue.Speak("Lift")
        )

        val sanitized = sanitizeCuesForMotivation(cues, appSettings)

        assertTrue(sanitized.hasMotivationCue)
        assertTrue(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.equals("Almost there", ignoreCase = true)
            }
        )
        assertTrue(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.equals("Lift", ignoreCase = true)
            }
        )
        assertFalse(sanitized.cues.any { it is TimerCue.PlayExactCounter })
        assertFalse(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.toIntOrNull() != null
            }
        )
    }

    @Test
    fun keepsCountingCuesWhenMotivationIsDisabled() {
        val appSettings = AppSettings(motivationEnabled = false)
        val cues = listOf(
            TimerCue.Speak("Keep going"),
            TimerCue.Speak("5"),
            TimerCue.PlayExactCounter(10)
        )

        val sanitized = sanitizeCuesForMotivation(cues, appSettings)

        assertFalse(sanitized.hasMotivationCue)
        assertTrue(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text == "5"
            }
        )
        assertTrue(sanitized.cues.any { it is TimerCue.PlayExactCounter })
    }

    @Test
    fun keepsLastOneCueAndPausesCounterEvenWhenMotivationIsDisabled() {
        val appSettings = AppSettings(motivationEnabled = false)
        val cues = listOf(
            TimerCue.Speak("Last one"),
            TimerCue.Speak("5"),
            TimerCue.PlayExactCounter(10),
            TimerCue.Speak("Lift")
        )

        val sanitized = sanitizeCuesForMotivation(cues, appSettings)

        assertTrue(sanitized.hasMotivationCue)
        assertTrue(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.equals("Last one", ignoreCase = true)
            }
        )
        assertFalse(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.toIntOrNull() != null
            }
        )
        assertTrue(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.equals("Lift", ignoreCase = true)
            }
        )
        assertFalse(sanitized.cues.any { it is TimerCue.PlayExactCounter })
        assertTrue(sanitized.deferredCues.any { it is TimerCue.PlayExactCounter })
    }

    @Test
    fun keepsCompletedCueUnchangedWhenMotivationVoiceIsCustomized() {
        val appSettings = AppSettings(
            motivationEnabled = true,
            motivationVoice = MotivationVoiceOption.ALMOST_THERE
        )
        val cues = listOf(TimerCue.Speak("Completed"))

        val sanitized = sanitizeCuesForMotivation(cues, appSettings)

        assertTrue(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.equals("Completed", ignoreCase = true)
            }
        )
        assertFalse(
            sanitized.cues.any {
                it is TimerCue.Speak && it.text.equals("Well done", ignoreCase = true)
            }
        )
    }
}
