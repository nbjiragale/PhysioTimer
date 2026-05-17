package com.niranjan.physiotimer.timer

import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTimerTest {
    @Test
    fun advancesThroughStepsAndReps() {
        val timer = SessionTimer(twoStepExercise(reps = 2))

        timer.start(0L)
        val secondStep = timer.tick(1_000L)
        assertEquals(0, secondStep.snapshot.repIndex)
        assertEquals(1, secondStep.snapshot.stepIndex)
        assertEquals("Hold", secondStep.snapshot.stepName)

        val nextRep = timer.tick(2_000L)
        assertEquals(1, nextRep.snapshot.repIndex)
        assertEquals(0, nextRep.snapshot.stepIndex)
        assertTrue(nextRep.spokenTexts().contains("Last one"))

        val complete = timer.tick(4_000L)
        assertEquals(TimerStatus.Complete, complete.snapshot.status)
        assertTrue(complete.spokenTexts().contains("Completed"))
    }

    @Test
    fun pauseAndResumeKeepRemainingTime() {
        val timer = SessionTimer(
            Exercise(
                name = "Hold",
                reps = 1,
                startDelaySeconds = 0,
                steps = listOf(ExerciseStep(name = "Hold", durationSeconds = 5, countAloudEnabled = false))
            )
        )

        timer.start(0L)
        assertEquals(3, timer.tick(2_000L).snapshot.remainingSeconds)
        assertEquals(3, timer.pause(2_000L).snapshot.remainingSeconds)
        assertEquals(3, timer.tick(5_000L).snapshot.remainingSeconds)
        timer.resume(5_000L)
        assertEquals(2, timer.tick(6_000L).snapshot.remainingSeconds)
    }

    @Test
    fun skipStepAndSkipRepMoveForward() {
        val timer = SessionTimer(twoStepExercise(reps = 3))

        timer.start(0L)
        val skippedStep = timer.skipStep(100L)
        assertEquals("Hold", skippedStep.snapshot.stepName)

        val skippedRep = timer.skipRep(200L)
        assertEquals(1, skippedRep.snapshot.repIndex)
        assertEquals(0, skippedRep.snapshot.stepIndex)
        assertTrue(skippedRep.spokenTexts().contains("Keep going"))
    }

    @Test
    fun keepGoingOnlyPlaysWhenMiddleRepStarts() {
        val timer = SessionTimer(singleStepExercise(reps = 10))

        timer.start(0L)
        assertEquals(emptyList<String>(), timer.tick(1_000L).spokenTexts())
        assertEquals(emptyList<String>(), timer.tick(2_000L).spokenTexts())
        assertEquals(emptyList<String>(), timer.tick(3_000L).spokenTexts())
        val fifthRep = timer.tick(4_000L)
        assertEquals(4, fifthRep.snapshot.repIndex)
        assertEquals(listOf("Keep going"), fifthRep.spokenTexts())
        assertEquals(emptyList<String>(), timer.tick(5_000L).spokenTexts())
    }

    @Test
    fun countAloudIncludesFinalDuration() {
        val timer = SessionTimer(
            Exercise(
                name = "Stretch",
                reps = 1,
                startDelaySeconds = 0,
                steps = listOf(
                    ExerciseStep(
                        name = "Stretch",
                        durationSeconds = 5,
                        countAloudEnabled = true,
                        countIntervalSeconds = 2
                    )
                )
            )
        )

        timer.start(0L)
        assertEquals(listOf("2"), timer.tick(2_000L).spokenTexts())
        assertEquals(listOf("4"), timer.tick(4_000L).spokenTexts())
        val finalTexts = timer.tick(5_000L).spokenTexts()
        assertTrue(finalTexts.contains("5"))
        assertTrue(finalTexts.contains("Completed"))
    }

    @Test
    fun startDelayRunsBeforeFirstStep() {
        val timer = SessionTimer(
            Exercise(
                name = "Leg Raise",
                reps = 1,
                startDelaySeconds = 3,
                steps = listOf(ExerciseStep(name = "Lift", durationSeconds = 2, countAloudEnabled = false))
            )
        )

        val start = timer.start(0L)
        assertTrue(start.snapshot.isPreparing)
        assertEquals("Starting exercise", start.snapshot.stepName)
        assertEquals(3, start.snapshot.remainingSeconds)
        assertTrue(start.spokenTexts().contains("Starting exercise"))

        val countdown = timer.tick(1_000L)
        assertTrue(countdown.snapshot.isPreparing)
        assertEquals(2, countdown.snapshot.remainingSeconds)
        assertEquals(listOf("2"), countdown.spokenTexts())

        val firstStep = timer.tick(3_000L)
        assertTrue(!firstStep.snapshot.isPreparing)
        assertEquals("Lift", firstStep.snapshot.stepName)
        assertTrue(firstStep.spokenTexts().contains("Lift"))
        assertTrue(!firstStep.spokenTexts().contains("Go"))
    }

    @Test
    fun startCountdownCountAloudCanBeDisabled() {
        val timer = SessionTimer(
            Exercise(
                name = "Leg Raise",
                reps = 1,
                startDelaySeconds = 3,
                startCountdownCountAloudEnabled = false,
                steps = listOf(ExerciseStep(name = "Lift", durationSeconds = 2, countAloudEnabled = false))
            )
        )

        timer.start(0L)
        assertTrue(timer.tick(1_000L).spokenTexts().isEmpty())
        assertTrue(timer.tick(2_000L).spokenTexts().isEmpty())
    }

    @Test
    fun startCountdownUsesCustomInterval() {
        val timer = SessionTimer(
            Exercise(
                name = "Leg Raise",
                reps = 1,
                startDelaySeconds = 5,
                startCountdownIntervalSeconds = 2,
                steps = listOf(ExerciseStep(name = "Lift", durationSeconds = 2, countAloudEnabled = false))
            )
        )

        timer.start(0L)
        assertTrue(timer.tick(1_000L).spokenTexts().isEmpty())
        assertEquals(listOf("3"), timer.tick(2_000L).spokenTexts())
        assertTrue(timer.tick(3_000L).spokenTexts().isEmpty())
        assertEquals(listOf("1"), timer.tick(4_000L).spokenTexts())
    }

    @Test
    fun delayedTicksCarryOverflowIntoCurrentPhase() {
        val timer = SessionTimer(
            Exercise(
                name = "Leg Raise",
                reps = 1,
                startDelaySeconds = 1,
                steps = listOf(
                    ExerciseStep(name = "Lift", durationSeconds = 2, countAloudEnabled = false),
                    ExerciseStep(name = "Hold", durationSeconds = 3, countAloudEnabled = false)
                )
            )
        )

        timer.start(0L)
        val midFirstStep = timer.tick(2_500L)
        assertEquals("Lift", midFirstStep.snapshot.stepName)
        assertEquals(500L, midFirstStep.snapshot.remainingMillis)

        val midSecondStep = timer.tick(3_200L)
        assertEquals("Hold", midSecondStep.snapshot.stepName)
        assertEquals(2_800L, midSecondStep.snapshot.remainingMillis)
    }

    @Test
    fun exactCounterDoesNotPlayWhenVoiceIsDisabled() {
        val timer = SessionTimer(
            Exercise(
                name = "Classic",
                reps = 1,
                startDelaySeconds = 0,
                voiceEnabled = false,
                steps = listOf(
                    ExerciseStep(
                        name = "Classic timer",
                        durationSeconds = 10,
                        countAloudEnabled = true
                    )
                )
            )
        )

        val start = timer.start(0L)
        assertTrue(start.cues.none { it is TimerCue.PlayExactCounter })
        assertTrue(start.spokenTexts().isEmpty())
    }

    private fun twoStepExercise(reps: Int): Exercise {
        return Exercise(
            name = "Leg Raise",
            reps = reps,
            startDelaySeconds = 0,
            steps = listOf(
                ExerciseStep(name = "Lift", durationSeconds = 1, countAloudEnabled = false),
                ExerciseStep(name = "Hold", durationSeconds = 1, countAloudEnabled = false)
            )
        )
    }

    private fun singleStepExercise(reps: Int): Exercise {
        return Exercise(
            name = "Leg Raise",
            reps = reps,
            startDelaySeconds = 0,
            steps = listOf(
                ExerciseStep(
                    name = "Lift",
                    durationSeconds = 1,
                    voicePromptEnabled = false,
                    countAloudEnabled = false
                )
            )
        )
    }

    private fun TimerUpdate.spokenTexts(): List<String> {
        return cues.filterIsInstance<TimerCue.Speak>().map { it.text }
    }
}
