package com.example.fitnesapp.domain.model

data class WorkoutPlanDay(
    val dayOfWeek: Int,
    val isRestDay: Boolean,
    val exercises: List<WorkoutPlanExercise>,
    val title: String = dayOfWeek.dayLabel()
)

enum class WorkoutStage {
    INPUT_SET,
    REST_TIMER,
    EXERCISE_FINISHED,
    WORKOUT_FINISHED
}

data class WorkoutPlanExercise(
    val id: Long = 0L,
    val dayOfWeek: Int,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroupTag: MuscleGroup = MuscleGroup.OTHER,
    val exerciseType: ExerciseType,
    val loadUnit: LoadUnit,
    val orderInDay: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val restSeconds: Int,
    val note: String = ""
)

data class WorkoutSession(
    val id: Long,
    val dayOfWeek: Int,
    val dayLabel: String,
    val startedAt: Long,
    val completedAt: Long?,
    val isCompleted: Boolean,
    val currentExerciseIndex: Int,
    val currentStage: WorkoutStage,
    val restTimerEndAt: Long?,
    val exercises: List<WorkoutSessionExercise>
)

data class WorkoutSessionExercise(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long?,
    val plannedExerciseId: Long?,
    val plannedExerciseName: String,
    val exerciseName: String,
    val muscleGroupTag: MuscleGroup,
    val exerciseType: ExerciseType,
    val loadUnit: LoadUnit,
    val orderInWorkout: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val restSeconds: Int,
    val note: String,
    val isReplacement: Boolean,
    val skipped: Boolean,
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val id: Long,
    val sessionExerciseId: Long,
    val orderInExercise: Int,
    val targetReps: Int?,
    val targetWeight: Double?,
    val actualReps: Int?,
    val actualWeight: Double?,
    val completed: Boolean,
    val skipped: Boolean,
    val updatedAt: Long
)

data class WorkoutComparison(
    val currentVolume: Double,
    val previousVolume: Double?,
    val message: String
)

data class TodayWorkoutDecision(
    val activeSessionId: Long? = null,
    val plannedDay: WorkoutPlanDay? = null,
    val message: String
)
