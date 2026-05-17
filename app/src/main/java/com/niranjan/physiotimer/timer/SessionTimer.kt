package com.niranjan.physiotimer.timer

import androidx.compose.runtime.Immutable
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseStep

private const val MILLIS_PER_SECOND = 1_000L

enum class TimerStatus {
    Idle,
    Running,
    Paused,
    Complete,
    Stopped
}

@Immutable
data class TimerSnapshot(
    val status: TimerStatus,
    val exerciseName: String,
    val repIndex: Int,
    val totalReps: Int,
    val stepIndex: Int,
    val totalSteps: Int,
    val stepName: String,
    val isPreparing: Boolean,
    val remainingMillis: Long,
    val stepDurationMillis: Long,
    val stepColorArgb: Long,
    val totalElapsedMillis: Long
) {
    val currentRep: Int = (repIndex + 1).coerceAtMost(totalReps)
    val remainingSeconds: Int = ((remainingMillis + 999L) / MILLIS_PER_SECOND).toInt()
}

sealed interface TimerCue {
    data class Speak(val text: String) : TimerCue
    data class PlayExactCounter(val durationSeconds: Int) : TimerCue
    data object Feedback : TimerCue
}

data class TimerUpdate(
    val snapshot: TimerSnapshot,
    val cues: List<TimerCue> = emptyList()
)

class SessionTimer(private val exercise: Exercise) {
    private var status = TimerStatus.Idle
    private var phase = TimerPhase.Exercising
    private var repIndex = 0
    private var stepIndex = 0
    private var phaseStartedAtMs = 0L
    private var elapsedBeforePauseMs = 0L
    private val spokenStartSeconds = mutableSetOf<Int>()
    private val spokenCounts = mutableSetOf<CountKey>()
    private val playedExactCounters = mutableSetOf<CounterKey>()

    fun start(nowMs: Long): TimerUpdate {
        repIndex = 0
        stepIndex = 0
        phase = if (exercise.startDelaySeconds > 0) TimerPhase.Preparing else TimerPhase.Exercising
        elapsedBeforePauseMs = 0L
        phaseStartedAtMs = nowMs
        spokenStartSeconds.clear()
        spokenCounts.clear()
        playedExactCounters.clear()
        status = TimerStatus.Running
        return if (phase == TimerPhase.Preparing) {
            TimerUpdate(snapshot(nowMs), startDelayCues())
        } else {
            TimerUpdate(snapshot(nowMs), startIntroCues() + stepStartCues(currentStep()))
        }
    }

    fun tick(nowMs: Long): TimerUpdate {
        if (status != TimerStatus.Running) return TimerUpdate(snapshot(nowMs))

        val cues = mutableListOf<TimerCue>()
        var elapsedMs = elapsedInCurrentPhase(nowMs)

        while (status == TimerStatus.Running && elapsedMs >= currentPhaseDurationMs()) {
            if (phase == TimerPhase.Exercising) {
                cues += countCuesUpTo(currentStep(), currentPhaseDurationMs())
            }
            val overflowMs = elapsedMs - currentPhaseDurationMs()
            advanceAfterPhase(cues)
            if (status == TimerStatus.Running) {
                elapsedBeforePauseMs = overflowMs
                phaseStartedAtMs = nowMs
                elapsedMs = overflowMs
            }
        }

        if (status == TimerStatus.Running && phase == TimerPhase.Preparing) {
            cues += startCountdownCuesUpTo(elapsedInCurrentPhase(nowMs))
        }
        if (status == TimerStatus.Running && phase == TimerPhase.Exercising) {
            cues += countCuesUpTo(currentStep(), elapsedInCurrentPhase(nowMs))
        }

        return TimerUpdate(snapshot(nowMs), cues)
    }

    fun pause(nowMs: Long, announce: Boolean = true): TimerUpdate {
        if (status == TimerStatus.Running) {
            elapsedBeforePauseMs = elapsedInCurrentPhase(nowMs)
            status = TimerStatus.Paused
            val cues = if (announce) speak("Paused") else emptyList()
            return TimerUpdate(snapshot(nowMs), cues)
        }
        return TimerUpdate(snapshot(nowMs))
    }

    fun resume(nowMs: Long, announce: Boolean = true): TimerUpdate {
        if (status == TimerStatus.Paused) {
            phaseStartedAtMs = nowMs
            status = TimerStatus.Running
            val cues = if (announce) speak("Resuming") else emptyList()
            return TimerUpdate(snapshot(nowMs), cues)
        }
        return TimerUpdate(snapshot(nowMs))
    }

    fun skipStep(nowMs: Long): TimerUpdate {
        if (status != TimerStatus.Running && status != TimerStatus.Paused) {
            return TimerUpdate(snapshot(nowMs))
        }
        val cues = mutableListOf<TimerCue>()
        if (phase == TimerPhase.Preparing) {
            beginFirstStep(cues)
        } else {
            advanceAfterStep(cues)
        }
        if (status == TimerStatus.Running || status == TimerStatus.Paused) {
            elapsedBeforePauseMs = 0L
            phaseStartedAtMs = nowMs
            status = TimerStatus.Running
        }
        return TimerUpdate(snapshot(nowMs), cues)
    }

    fun skipRep(nowMs: Long): TimerUpdate {
        if (status != TimerStatus.Running && status != TimerStatus.Paused) {
            return TimerUpdate(snapshot(nowMs))
        }
        val cues = mutableListOf<TimerCue>()
        if (phase == TimerPhase.Preparing) {
            beginFirstStep(cues)
        } else if (repIndex >= exercise.reps - 1) {
            complete(cues)
        } else {
            repIndex += 1
            stepIndex = 0
            phase = TimerPhase.Exercising
            elapsedBeforePauseMs = 0L
            phaseStartedAtMs = nowMs
            status = TimerStatus.Running
            cues += nextRepCues()
            cues += stepStartCues(currentStep())
        }
        return TimerUpdate(snapshot(nowMs), cues)
    }

    fun stop(nowMs: Long): TimerUpdate {
        if (status == TimerStatus.Running) {
            elapsedBeforePauseMs = elapsedInCurrentPhase(nowMs)
        }
        status = TimerStatus.Stopped
        return TimerUpdate(snapshot(nowMs))
    }

    fun snapshot(nowMs: Long): TimerSnapshot {
        val step = currentStep()
        val durationMs = currentPhaseDurationMs()
        val elapsedMs = when (status) {
            TimerStatus.Running -> elapsedInCurrentPhase(nowMs)
            else -> elapsedBeforePauseMs
        }
        val remainingMs = (durationMs - elapsedMs).coerceIn(0L, durationMs)
        val totalElapsed = elapsedInSession(nowMs)
        return TimerSnapshot(
            status = status,
            exerciseName = exercise.name,
            repIndex = repIndex,
            totalReps = exercise.reps,
            stepIndex = stepIndex,
            totalSteps = exercise.steps.size,
            stepName = if (phase == TimerPhase.Preparing) "Starting exercise" else step.name,
            isPreparing = phase == TimerPhase.Preparing,
            remainingMillis = remainingMs,
            stepDurationMillis = durationMs,
            stepColorArgb = step.colorArgb,
            totalElapsedMillis = totalElapsed
        )
    }

    private fun advanceAfterPhase(cues: MutableList<TimerCue>) {
        if (phase == TimerPhase.Preparing) {
            beginFirstStep(cues)
        } else {
            advanceAfterStep(cues)
        }
    }

    private fun beginFirstStep(cues: MutableList<TimerCue>) {
        phase = TimerPhase.Exercising
        stepIndex = 0
        elapsedBeforePauseMs = 0L
        cues += stepStartCues(currentStep())
    }

    private fun advanceAfterStep(cues: MutableList<TimerCue>) {
        if (stepIndex < exercise.steps.lastIndex) {
            stepIndex += 1
            phase = TimerPhase.Exercising
            elapsedBeforePauseMs = 0L
            cues += stepStartCues(currentStep())
            return
        }

        if (repIndex < exercise.reps - 1) {
            repIndex += 1
            stepIndex = 0
            phase = TimerPhase.Exercising
            elapsedBeforePauseMs = 0L
            cues += nextRepCues()
            cues += stepStartCues(currentStep())
            return
        }

        complete(cues)
    }

    private fun complete(cues: MutableList<TimerCue>) {
        status = TimerStatus.Complete
        elapsedBeforePauseMs = currentPhaseDurationMs()
        cues += speak("Completed")
    }

    private fun startDelayCues(): List<TimerCue> {
        return buildList {
            add(TimerCue.Feedback)
            addAll(startIntroCues())
        }
    }

    private fun startIntroCues(): List<TimerCue> {
        return buildList {
            addAll(speak("Starting exercise"))
            addAll(speak("Lie down or sit"))
        }
    }

    private fun startCountdownCuesUpTo(elapsedMs: Long): List<TimerCue> {
        if (!exercise.voiceEnabled || !exercise.startCountdownCountAloudEnabled) return emptyList()

        val elapsedSeconds = (elapsedMs / MILLIS_PER_SECOND).toInt()
        return (1..elapsedSeconds).mapNotNull { secondsElapsed ->
            val remaining = exercise.startDelaySeconds - secondsElapsed
            val shouldSpeak = secondsElapsed % exercise.startCountdownIntervalSeconds == 0
            if (remaining > 0 && shouldSpeak && spokenStartSeconds.add(remaining)) {
                TimerCue.Speak(remaining.toString())
            } else {
                null
            }
        }
    }

    private fun stepStartCues(step: ExerciseStep): List<TimerCue> {
        return buildList {
            add(TimerCue.Feedback)
            if (exercise.voiceEnabled && step.voicePromptEnabled) {
                add(TimerCue.Speak(step.name))
            }
            if (shouldUseExactCounter(step) && playedExactCounters.add(CounterKey(repIndex, stepIndex))) {
                add(TimerCue.PlayExactCounter(step.durationSeconds))
            }
        }
    }

    private fun countCuesUpTo(step: ExerciseStep, elapsedMs: Long): List<TimerCue> {
        if (!exercise.voiceEnabled || !step.countAloudEnabled) return emptyList()
        if (shouldUseExactCounter(step)) return emptyList()

        val elapsedSeconds = (elapsedMs / MILLIS_PER_SECOND).toInt()
        return countSchedule(step).filter { seconds ->
            seconds <= elapsedSeconds && spokenCounts.add(CountKey(repIndex, stepIndex, seconds))
        }.map { seconds ->
            TimerCue.Speak(seconds.toString())
        }
    }

    private fun shouldUseExactCounter(step: ExerciseStep): Boolean {
        return exercise.voiceEnabled && step.countAloudEnabled && step.durationSeconds in setOf(10, 20)
    }

    private fun countSchedule(step: ExerciseStep): List<Int> {
        val values = mutableListOf<Int>()
        var next = step.countIntervalSeconds
        while (next <= step.durationSeconds) {
            values += next
            next += step.countIntervalSeconds
        }
        if (values.lastOrNull() != step.durationSeconds) {
            values += step.durationSeconds
        }
        return values
    }

    private fun speak(text: String): List<TimerCue> {
        return if (exercise.voiceEnabled) listOf(TimerCue.Speak(text)) else emptyList()
    }

    private fun nextRepCues(): List<TimerCue> {
        val currentRep = repIndex + 1
        return when {
            currentRep == exercise.reps -> speak("Last one")
            currentRep == middleRep() -> speak("Keep going")
            else -> emptyList()
        }
    }

    private fun middleRep(): Int = (exercise.reps + 1) / 2

    private fun elapsedInCurrentPhase(nowMs: Long): Long {
        return (elapsedBeforePauseMs + nowMs - phaseStartedAtMs).coerceAtLeast(0L)
    }

    private fun elapsedInSession(nowMs: Long): Long {
        val totalDurationMs = exercise.totalSeconds * MILLIS_PER_SECOND
        if (status == TimerStatus.Complete) {
            return totalDurationMs
        }

        val currentPhaseElapsed = when (status) {
            TimerStatus.Running -> elapsedInCurrentPhase(nowMs)
            else -> elapsedBeforePauseMs
        }

        val stepPrefixMs = exercise.steps
            .take(stepIndex.coerceIn(0, exercise.steps.size))
            .sumOf { it.durationSeconds * MILLIS_PER_SECOND }
        val repDurationMs = exercise.steps.sumOf { it.durationSeconds * MILLIS_PER_SECOND }
        val completedRepMs = repIndex.coerceAtLeast(0) * repDurationMs
        val baseMs = if (phase == TimerPhase.Preparing) {
            0L
        } else {
            exercise.startDelaySeconds * MILLIS_PER_SECOND + completedRepMs + stepPrefixMs
        }
        return (baseMs + currentPhaseElapsed).coerceIn(0L, totalDurationMs)
    }

    private fun currentPhaseDurationMs(): Long {
        return when (phase) {
            TimerPhase.Preparing -> exercise.startDelaySeconds * MILLIS_PER_SECOND
            TimerPhase.Exercising -> currentStep().durationSeconds * MILLIS_PER_SECOND
        }
    }

    private fun currentStep(): ExerciseStep = exercise.steps[stepIndex.coerceIn(exercise.steps.indices)]

    private enum class TimerPhase {
        Preparing,
        Exercising
    }

    private data class CountKey(val repIndex: Int, val stepIndex: Int, val seconds: Int)
    private data class CounterKey(val repIndex: Int, val stepIndex: Int)
}
