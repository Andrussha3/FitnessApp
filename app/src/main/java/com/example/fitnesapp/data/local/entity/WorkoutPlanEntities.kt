package com.example.fitnesapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Embedded
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.LoadUnit

@Entity(tableName = "workout_plan_days")
data class WorkoutPlanDayEntity(
    @PrimaryKey val dayOfWeek: Int,
    val isRestDay: Boolean,
    val title: String
)

@Entity(
    tableName = "workout_plan_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanDayEntity::class,
            parentColumns = ["dayOfWeek"],
            childColumns = ["dayOfWeek"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("dayOfWeek"), Index("exerciseId")]
)
data class WorkoutPlanExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dayOfWeek: Int,
    val exerciseId: Long,
    val orderInDay: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val restSeconds: Int,
    val note: String
)

data class WorkoutPlanDayWithExercises(
    @Embedded val day: WorkoutPlanDayEntity,
    @Relation(parentColumn = "dayOfWeek", entityColumn = "dayOfWeek")
    val exercises: List<WorkoutPlanExerciseEntity>
)

data class WorkoutPlanExerciseResolved(
    val id: Long,
    val dayOfWeek: Int,
    val exerciseId: Long,
    val orderInDay: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val restSeconds: Int,
    val note: String,
    val exerciseName: String,
    val muscleGroupTag: com.example.fitnesapp.domain.model.MuscleGroup,
    val exerciseType: ExerciseType,
    val loadUnit: LoadUnit
)
