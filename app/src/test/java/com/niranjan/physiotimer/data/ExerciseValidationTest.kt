package com.niranjan.physiotimer.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseValidationTest {
    @Test
    fun validExerciseHasNoErrors() {
        val exercise = Exercise(
            name = "Leg Raise",
            reps = 10,
            steps = listOf(ExerciseStep(name = "Lift", durationSeconds = 10, countIntervalSeconds = 1))
        )

        assertTrue(validateExercise(exercise).isEmpty())
    }

    @Test
    fun invalidExerciseReportsBlockingErrors() {
        val exercise = Exercise(
            name = "",
            reps = 0,
            startDelaySeconds = -1,
            steps = listOf(ExerciseStep(name = "", durationSeconds = 0, countIntervalSeconds = 5))
        )

        val errors = validateExercise(exercise)
        assertTrue(errors.any { it.contains("name", ignoreCase = true) })
        assertTrue(errors.any { it.contains("Reps") })
        assertTrue(errors.any { it.contains("countdown") })
        assertTrue(errors.any { it.contains("duration") })
        assertTrue(errors.any { it.contains("interval") })
    }
}
