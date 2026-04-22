package com.example.fitnesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fitnesapp.data.local.entity.SessionExerciseEntity
import com.example.fitnesapp.data.local.entity.WorkoutSessionEntity
import com.example.fitnesapp.data.local.entity.WorkoutSessionWithDetails
import com.example.fitnesapp.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveSession(): Flow<WorkoutSessionWithDetails?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionWithDetails?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    fun observeSession(sessionId: Long): Flow<WorkoutSessionWithDetails?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: Long): WorkoutSessionWithDetails?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY startedAt DESC")
    fun observeHistory(): Flow<List<WorkoutSessionWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercises(exercises: List<SessionExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Query(
        "UPDATE workout_sets SET actualReps = :actualReps, actualWeight = :actualWeight, completed = :completed, skipped = :skipped, updatedAt = :updatedAt WHERE id = :setId"
    )
    suspend fun updateSet(setId: Long, actualReps: Int?, actualWeight: Double?, completed: Boolean, skipped: Boolean, updatedAt: Long)

    @Query("UPDATE session_exercises SET skipped = :skipped WHERE id = :sessionExerciseId")
    suspend fun markExerciseSkipped(sessionExerciseId: Long, skipped: Boolean)

    @Query(
        "UPDATE session_exercises SET exerciseId = :exerciseId, exerciseName = :exerciseName, muscleGroupTag = :muscleGroupTag, exerciseType = :exerciseType, loadUnit = :loadUnit, isReplacement = 1 WHERE id = :sessionExerciseId"
    )
    suspend fun replaceSessionExercise(
        sessionExerciseId: Long,
        exerciseId: Long?,
        exerciseName: String,
        muscleGroupTag: com.example.fitnesapp.domain.model.MuscleGroup,
        exerciseType: com.example.fitnesapp.domain.model.ExerciseType,
        loadUnit: com.example.fitnesapp.domain.model.LoadUnit
    )

    @Query("UPDATE workout_sessions SET currentExerciseIndex = :exerciseIndex WHERE id = :sessionId")
    suspend fun setCurrentExercise(sessionId: Long, exerciseIndex: Int)

    @Query("UPDATE workout_sessions SET currentExerciseIndex = :exerciseIndex, currentStage = 'INPUT_SET', restTimerEndAt = NULL WHERE id = :sessionId")
    suspend fun advanceToNextExercise(sessionId: Long, exerciseIndex: Int)

    @Query("UPDATE workout_sessions SET currentStage = :stage WHERE id = :sessionId")
    suspend fun setStage(sessionId: Long, stage: com.example.fitnesapp.domain.model.WorkoutStage)

    @Query("UPDATE workout_sessions SET restTimerEndAt = :restTimerEndAt WHERE id = :sessionId")
    suspend fun setRestTimer(sessionId: Long, restTimerEndAt: Long?)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId AND isCompleted = 0")
    suspend fun deleteActiveSession(sessionId: Long)

    @Query("UPDATE workout_sessions SET isCompleted = 1, completedAt = :completedAt, restTimerEndAt = NULL, currentStage = 'WORKOUT_FINISHED' WHERE id = :sessionId")
    suspend fun completeSession(sessionId: Long, completedAt: Long)

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 AND dayOfWeek = :dayOfWeek ORDER BY completedAt DESC LIMIT 2")
    @Transaction
    suspend fun getLastCompletedSessionsForDay(dayOfWeek: Int): List<WorkoutSessionWithDetails>

    @Query("DELETE FROM workout_sets")
    suspend fun clearSets()

    @Query("DELETE FROM session_exercises")
    suspend fun clearSessionExercises()

    @Query("DELETE FROM workout_sessions")
    suspend fun clearSessions()
}
