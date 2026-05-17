package com.niranjan.physiotimer.data

import androidx.room.withTransaction
import com.niranjan.physiotimer.data.local.ExerciseEntity
import com.niranjan.physiotimer.data.local.ExerciseWithSteps
import com.niranjan.physiotimer.data.local.PhysioDatabase
import com.niranjan.physiotimer.data.local.SessionRecordEntity
import com.niranjan.physiotimer.data.local.StepEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(private val database: PhysioDatabase) {
    private val dao = database.exerciseDao()

    fun observeExercises(): Flow<List<Exercise>> {
        return dao.observeExercises().map { items ->
            items.map { it.toDomain() }
        }
    }

    fun observeRecentExercises(): Flow<List<Exercise>> {
        return dao.observeRecentExercises().map { items ->
            items.map { it.toDomain() }
        }
    }

    fun observeExercise(id: Long): Flow<Exercise?> {
        return dao.observeExercise(id).map { it?.toDomain() }
    }

    fun observeSessionRecords(): Flow<List<SessionRecord>> {
        return dao.observeSessionRecords().map { items ->
            items.map { it.toDomain() }
        }
    }

    suspend fun saveExercise(exercise: Exercise): Long {
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val exerciseId = if (exercise.id == 0L) {
                dao.insertExercise(
                    ExerciseEntity(
                        name = exercise.name.trim(),
                        imageUri = exercise.imageUri,
                        reps = exercise.reps,
                        startDelaySeconds = exercise.startDelaySeconds,
                        startCountdownCountAloudEnabled = exercise.startCountdownCountAloudEnabled,
                        startCountdownIntervalSeconds = exercise.startCountdownIntervalSeconds,
                        voiceEnabled = exercise.voiceEnabled,
                        vibrationEnabled = exercise.vibrationEnabled,
                        beepEnabled = exercise.beepEnabled,
                        lastStartedAt = exercise.lastStartedAt,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                val current = dao.getExercise(exercise.id)
                dao.updateExercise(
                    ExerciseEntity(
                        id = exercise.id,
                        name = exercise.name.trim(),
                        imageUri = exercise.imageUri,
                        reps = exercise.reps,
                        startDelaySeconds = exercise.startDelaySeconds,
                        startCountdownCountAloudEnabled = exercise.startCountdownCountAloudEnabled,
                        startCountdownIntervalSeconds = exercise.startCountdownIntervalSeconds,
                        voiceEnabled = exercise.voiceEnabled,
                        vibrationEnabled = exercise.vibrationEnabled,
                        beepEnabled = exercise.beepEnabled,
                        lastStartedAt = current?.lastStartedAt ?: exercise.lastStartedAt,
                        createdAt = current?.createdAt ?: now,
                        updatedAt = now
                    )
                )
                exercise.id
            }

            dao.deleteStepsForExercise(exerciseId)
            dao.insertSteps(
                exercise.steps.mapIndexed { index, step ->
                    StepEntity(
                        exerciseId = exerciseId,
                        position = index,
                        name = step.name.trim(),
                        durationSeconds = step.durationSeconds,
                        voicePromptEnabled = step.voicePromptEnabled,
                        countAloudEnabled = step.countAloudEnabled,
                        countIntervalSeconds = step.countIntervalSeconds,
                        colorArgb = step.colorArgb
                    )
                }
            )
            exerciseId
        }
    }

    suspend fun deleteExercise(id: Long) {
        dao.deleteExercise(id)
    }

    suspend fun markExerciseStarted(id: Long) {
        if (id != 0L) {
            dao.updateLastStartedAt(id, System.currentTimeMillis())
        }
    }

    suspend fun startSession(exercise: Exercise): Long {
        val now = System.currentTimeMillis()
        if (exercise.id != 0L) {
            dao.updateLastStartedAt(exercise.id, now)
        }
        return dao.insertSessionRecord(
            SessionRecordEntity(
                exerciseId = exercise.id.takeIf { it != 0L },
                exerciseName = exercise.name.ifBlank { "Classic timer" },
                startedAt = now,
                endedAt = 0L,
                elapsedSeconds = 0,
                plannedSeconds = exercise.totalSeconds,
                completed = false
            )
        )
    }

    suspend fun finishSession(
        sessionId: Long,
        elapsedSeconds: Int,
        completed: Boolean
    ) {
        if (sessionId == 0L) return
        dao.finishSessionRecord(
            sessionId = sessionId,
            endedAt = System.currentTimeMillis(),
            elapsedSeconds = elapsedSeconds.coerceAtLeast(0),
            completed = completed
        )
    }
}

private fun ExerciseWithSteps.toDomain(): Exercise {
    return Exercise(
        id = exercise.id,
        name = exercise.name,
        imageUri = exercise.imageUri,
        reps = exercise.reps,
        startDelaySeconds = exercise.startDelaySeconds,
        startCountdownCountAloudEnabled = exercise.startCountdownCountAloudEnabled,
        startCountdownIntervalSeconds = exercise.startCountdownIntervalSeconds,
        voiceEnabled = exercise.voiceEnabled,
        vibrationEnabled = exercise.vibrationEnabled,
        beepEnabled = exercise.beepEnabled,
        lastStartedAt = exercise.lastStartedAt,
        steps = steps.sortedBy { it.position }.map { step ->
            ExerciseStep(
                id = step.id,
                name = step.name,
                durationSeconds = step.durationSeconds,
                voicePromptEnabled = step.voicePromptEnabled,
                countAloudEnabled = step.countAloudEnabled,
                countIntervalSeconds = step.countIntervalSeconds,
                colorArgb = step.colorArgb
            )
        }
    )
}

private fun SessionRecordEntity.toDomain(): SessionRecord {
    return SessionRecord(
        id = id,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        startedAt = startedAt,
        endedAt = endedAt,
        elapsedSeconds = elapsedSeconds,
        plannedSeconds = plannedSeconds,
        completed = completed
    )
}
