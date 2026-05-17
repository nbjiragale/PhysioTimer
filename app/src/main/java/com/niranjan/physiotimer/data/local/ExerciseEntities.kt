package com.niranjan.physiotimer.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val imageUri: String?,
    val reps: Int,
    val startDelaySeconds: Int,
    val startCountdownCountAloudEnabled: Boolean,
    val startCountdownIntervalSeconds: Int,
    val voiceEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val beepEnabled: Boolean,
    val lastStartedAt: Long,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val position: Int,
    val name: String,
    val durationSeconds: Int,
    val voicePromptEnabled: Boolean,
    val countAloudEnabled: Boolean,
    val countIntervalSeconds: Int,
    val colorArgb: Long
)

@Entity(
    tableName = "session_records",
    indices = [Index("startedAt"), Index("exerciseId")]
)
data class SessionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long?,
    val exerciseName: String,
    val startedAt: Long,
    val endedAt: Long,
    val elapsedSeconds: Int,
    val plannedSeconds: Int,
    val completed: Boolean
)

data class ExerciseWithSteps(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val steps: List<StepEntity>
)
