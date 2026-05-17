package com.niranjan.physiotimer.data

import androidx.compose.runtime.Immutable

@Immutable
data class Exercise(
    val id: Long = 0,
    val name: String = "",
    val imageUri: String? = null,
    val reps: Int = 10,
    val startDelaySeconds: Int = 5,
    val startCountdownCountAloudEnabled: Boolean = true,
    val startCountdownIntervalSeconds: Int = 1,
    val voiceEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val beepEnabled: Boolean = true,
    val lastStartedAt: Long = 0,
    val steps: List<ExerciseStep> = defaultSteps()
) {
    val workSeconds: Int
        get() = steps.sumOf { it.durationSeconds } * reps

    val totalSeconds: Int
        get() = startDelaySeconds + workSeconds
}

@Immutable
data class ExerciseStep(
    val id: Long = 0,
    val name: String = "",
    val durationSeconds: Int = 10,
    val voicePromptEnabled: Boolean = true,
    val countAloudEnabled: Boolean = true,
    val countIntervalSeconds: Int = 1,
    val colorArgb: Long = StepColors.first()
)

@Immutable
data class SessionRecord(
    val id: Long = 0,
    val exerciseId: Long? = null,
    val exerciseName: String = "",
    val startedAt: Long = 0,
    val endedAt: Long = 0,
    val elapsedSeconds: Int = 0,
    val plannedSeconds: Int = 0,
    val completed: Boolean = false
)

object StepColors {
    val palette = listOf(
        0xFF6F8F64L,
        0xFF8E72A0L,
        0xFFC47A5BL,
        0xFF6F9CADL,
        0xFF8A8F55L,
        0xFFA8875FL
    )

    fun first(): Long = palette.first()
}

fun defaultSteps(): List<ExerciseStep> = listOf(
    ExerciseStep(name = "Lift", durationSeconds = 10, colorArgb = StepColors.palette[0]),
    ExerciseStep(name = "Hold", durationSeconds = 10, colorArgb = StepColors.palette[1]),
    ExerciseStep(
        name = "Relax",
        durationSeconds = 5,
        countAloudEnabled = false,
        colorArgb = StepColors.palette[2]
    )
)

fun validateExercise(exercise: Exercise): List<String> {
    val errors = mutableListOf<String>()
    if (exercise.name.trim().isEmpty()) errors += "Exercise name is required."
    if (exercise.reps < 1) errors += "Reps must be at least 1."
    if (exercise.startDelaySeconds < 0) errors += "Start countdown cannot be negative."
    if (exercise.startDelaySeconds > 0) {
        if (exercise.startCountdownIntervalSeconds < 1) {
            errors += "Start countdown interval must be at least 1 second."
        }
        if (exercise.startCountdownIntervalSeconds > exercise.startDelaySeconds) {
            errors += "Start countdown interval cannot be longer than start countdown."
        }
    }
    if (exercise.steps.isEmpty()) errors += "Add at least one step."

    exercise.steps.forEachIndexed { index, step ->
        val label = "Step ${index + 1}"
        if (step.name.trim().isEmpty()) errors += "$label needs a name."
        if (step.durationSeconds < 1) errors += "$label duration must be at least 1 second."
        if (step.countIntervalSeconds < 1) errors += "$label count interval must be at least 1 second."
        if (step.countIntervalSeconds > step.durationSeconds) {
            errors += "$label count interval cannot be longer than duration."
        }
    }
    return errors
}
