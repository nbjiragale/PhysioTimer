package com.niranjan.physiotimer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Transaction
    @Query("SELECT * FROM exercises ORDER BY updatedAt DESC")
    fun observeExercises(): Flow<List<ExerciseWithSteps>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE lastStartedAt > 0 ORDER BY lastStartedAt DESC")
    fun observeRecentExercises(): Flow<List<ExerciseWithSteps>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeExercise(id: Long): Flow<ExerciseWithSteps?>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExercise(id: Long): ExerciseEntity?

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM steps WHERE exerciseId = :exerciseId")
    suspend fun deleteStepsForExercise(exerciseId: Long)

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    @Query("UPDATE exercises SET lastStartedAt = :startedAt WHERE id = :exerciseId")
    suspend fun updateLastStartedAt(exerciseId: Long, startedAt: Long)

    @Query("SELECT * FROM session_records ORDER BY startedAt DESC")
    fun observeSessionRecords(): Flow<List<SessionRecordEntity>>

    @Insert
    suspend fun insertSessionRecord(record: SessionRecordEntity): Long

    @Query(
        """
        UPDATE session_records
        SET endedAt = :endedAt,
            elapsedSeconds = :elapsedSeconds,
            completed = :completed
        WHERE id = :sessionId
        """
    )
    suspend fun finishSessionRecord(
        sessionId: Long,
        endedAt: Long,
        elapsedSeconds: Int,
        completed: Boolean
    )
}
