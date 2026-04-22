package com.example.fitnesapp.domain.model

enum class RecommendationAction {
    INCREASE,
    KEEP,
    DECREASE,
    NONE
}

enum class PersonalRecordType {
    MAX_WEIGHT,
    MAX_REPS,
    MAX_EXERCISE_VOLUME,
    MAX_WORKOUT_VOLUME
}

data class ProgressionRecommendation(
    val id: Long = 0L,
    val sessionId: Long,
    val exerciseId: Long?,
    val exerciseName: String,
    val action: RecommendationAction,
    val currentWeight: Double?,
    val suggestedWeight: Double?,
    val message: String
)

data class PersonalRecord(
    val id: Long = 0L,
    val sessionId: Long,
    val exerciseId: Long?,
    val exerciseName: String,
    val recordType: PersonalRecordType,
    val value: Double,
    val achievedAt: Long
)

data class WorkoutHistoryItem(
    val sessionId: Long,
    val title: String,
    val startedAt: Long,
    val completedAt: Long,
    val durationMinutes: Int,
    val exerciseCount: Int,
    val completedSetsCount: Int,
    val totalVolume: Double,
    val comparisonLabel: String
)

data class WorkoutHistoryDetails(
    val session: WorkoutSession,
    val durationMinutes: Int,
    val totalVolume: Double,
    val comparisonMessage: String,
    val recommendations: List<ProgressionRecommendation>,
    val personalRecords: List<PersonalRecord>
)

fun WorkoutSession.strengthVolume(): Double = exercises
    .filter { it.exerciseType == ExerciseType.STRENGTH && !it.skipped }
    .flatMap { it.sets }
    .filter { it.completed && !it.skipped }
    .sumOf { (it.actualWeight ?: 0.0) * (it.actualReps ?: 0) }

fun WorkoutSession.durationMinutes(): Int {
    val end = completedAt ?: startedAt
    return ((end - startedAt) / 60000L).toInt().coerceAtLeast(1)
}

fun WorkoutSession.completedSetsCount(): Int = exercises.flatMap { it.sets }.count { it.completed && !it.skipped }
