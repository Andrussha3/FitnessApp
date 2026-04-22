package com.example.fitnesapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.LoadUnit
import com.example.fitnesapp.domain.model.WorkoutStage

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dayOfWeek: Int,
    val dayLabel: String,
    val startedAt: Long,
    val completedAt: Long?,
    val isCompleted: Boolean,
    val currentExerciseIndex: Int,
    val currentStage: WorkoutStage,
    val restTimerEndAt: Long?
)

@Entity(
    tableName = "session_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SessionExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val exerciseId: Long?,
    val plannedExerciseId: Long? = null,
    val plannedExerciseName: String = "",
    val exerciseName: String,
    val muscleGroupTag: com.example.fitnesapp.domain.model.MuscleGroup = com.example.fitnesapp.domain.model.MuscleGroup.OTHER,
    val exerciseType: ExerciseType,
    val loadUnit: LoadUnit,
    val orderInWorkout: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val restSeconds: Int,
    val note: String,
    val isReplacement: Boolean = false,
    val skipped: Boolean = false
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionExerciseId: Long,
    val orderInExercise: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val actualReps: Int?,
    val actualWeight: Double?,
    val completed: Boolean = false,
    val skipped: Boolean = false,
    val updatedAt: Long
)

data class SessionExerciseWithSets(
    @Embedded val exercise: SessionExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionExerciseId")
    val sets: List<WorkoutSetEntity>
)

data class WorkoutSessionWithDetails(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId", entity = SessionExerciseEntity::class)
    val exercises: List<SessionExerciseWithSets>
)
