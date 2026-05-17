package com.niranjan.physiotimer.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import com.niranjan.physiotimer.data.Exercise
import com.niranjan.physiotimer.data.ExerciseStep
import com.niranjan.physiotimer.data.StepColors

internal fun displayExercises(saved: List<Exercise>): List<Exercise> {
    if (saved.isNotEmpty()) return saved
    return listOf(
        Exercise(name = "Leg Raise", reps = 10, steps = sampleSteps(4)),
        Exercise(name = "Shoulder Stretch", reps = 8, steps = sampleSteps(3)),
        Exercise(name = "Back Mobility", reps = 6, steps = sampleSteps(5)),
        Exercise(name = "Knee Strength", reps = 12, steps = sampleSteps(4))
    )
}

internal fun sampleSteps(count: Int): List<ExerciseStep> {
    val names = listOf("Lift", "Hold", "Relax", "Pause", "Rest")
    val durations = listOf(10, 10, 5, 3, 6)
    return List(count) { index ->
        ExerciseStep(
            name = names[index % names.size],
            durationSeconds = durations[index % durations.size],
            colorArgb = StepColors.palette[index % StepColors.palette.size]
        )
    }
}

internal fun exerciseAccent(exercise: Exercise): Color = when {
    exercise.name.contains("shoulder", ignoreCase = true) -> WellnessColors.Lavender600
    exercise.name.contains("back", ignoreCase = true) -> WellnessColors.SkyMist500
    exercise.name.contains("knee", ignoreCase = true) -> WellnessColors.Clay400
    else -> WellnessColors.Sage600
}

internal fun List<Exercise>.filterByQuery(query: String): List<Exercise> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { exercise ->
        exercise.name.contains(normalized, ignoreCase = true) ||
            exercise.steps.any { it.name.contains(normalized, ignoreCase = true) }
    }
}

internal fun formatExerciseMeta(exercise: Exercise): String {
    val stepPreview = exercise.steps
        .map { it.name.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .take(3)
    if (stepPreview.isEmpty()) return "No steps added yet"
    val suffix = if (exercise.steps.size > stepPreview.size) "..." else ""
    return "${stepPreview.joinToString(" • ")}$suffix"
}

internal fun formatRecentMeta(exercise: Exercise): String {
    val recent = formatRelativeTime(exercise.lastStartedAt)
    return if (recent.isEmpty()) formatExerciseMeta(exercise) else "$recent - ${formatClock(exercise.totalSeconds)} total"
}

internal fun List<ExerciseStep>.replaceAt(index: Int, step: ExerciseStep): List<ExerciseStep> =
    mapIndexed { itemIndex, item -> if (itemIndex == index) step else item }

internal fun composeColor(argb: Long): Color = Color(argb.toInt())

internal fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

internal fun timerNowMs(): Long = SystemClock.elapsedRealtime()

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val elapsedSeconds = ((System.currentTimeMillis() - timestamp) / 1000L).coerceAtLeast(0L)
    return when {
        elapsedSeconds < 60 -> "Started just now"
        elapsedSeconds < 3600 -> "Started ${elapsedSeconds / 60} min ago"
        elapsedSeconds < 86_400 -> "Started ${elapsedSeconds / 3600} hr ago"
        elapsedSeconds < 172_800 -> "Started yesterday"
        else -> "Started ${elapsedSeconds / 86_400} days ago"
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
