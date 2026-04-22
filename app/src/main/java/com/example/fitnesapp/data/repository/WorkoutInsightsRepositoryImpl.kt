package com.example.fitnesapp.data.repository

import com.example.fitnesapp.data.toDomain
import com.example.fitnesapp.data.toEntity
import com.example.fitnesapp.data.local.dao.InsightsDao
import com.example.fitnesapp.data.local.dao.SessionDao
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.PersonalRecord
import com.example.fitnesapp.domain.model.PersonalRecordType
import com.example.fitnesapp.domain.model.ProgressionRecommendation
import com.example.fitnesapp.domain.model.RecommendationAction
import com.example.fitnesapp.domain.model.WorkoutHistoryDetails
import com.example.fitnesapp.domain.model.WorkoutHistoryItem
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.model.completedSetsCount
import com.example.fitnesapp.domain.model.durationMinutes
import com.example.fitnesapp.domain.model.strengthVolume
import com.example.fitnesapp.domain.repository.WorkoutInsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutInsightsRepositoryImpl(
    private val sessionDao: SessionDao,
    private val insightsDao: InsightsDao
) : WorkoutInsightsRepository {

    override fun observeWorkoutHistory(): Flow<List<WorkoutHistoryItem>> = sessionDao.observeHistory().map { entities ->
        val sessions = entities.map { it.toDomain() }.sortedByDescending { it.completedAt ?: it.startedAt }
        sessions.mapIndexed { index, session ->
            val previous = sessions.drop(index + 1).firstOrNull { it.dayOfWeek == session.dayOfWeek }
            WorkoutHistoryItem(
                sessionId = session.id,
                title = session.dayLabel,
                startedAt = session.startedAt,
                completedAt = session.completedAt ?: session.startedAt,
                durationMinutes = session.durationMinutes(),
                exerciseCount = session.exercises.size,
                completedSetsCount = session.completedSetsCount(),
                totalVolume = session.strengthVolume(),
                comparisonLabel = compareLabel(session.strengthVolume(), previous?.strengthVolume())
            )
        }
    }

    override suspend fun getWorkoutHistoryDetails(sessionId: Long): WorkoutHistoryDetails? {
        val session = sessionDao.getSession(sessionId)?.toDomain() ?: return null
        val history = sessionDao.getLastCompletedSessionsForDay(session.dayOfWeek).map { it.toDomain() }
        val previous = history.firstOrNull { it.id != session.id }
        return WorkoutHistoryDetails(
            session = session,
            durationMinutes = session.durationMinutes(),
            totalVolume = session.strengthVolume(),
            comparisonMessage = compareMessage(session.strengthVolume(), previous?.strengthVolume()),
            recommendations = insightsDao.getRecommendationsForSession(sessionId).map { it.toDomain() },
            personalRecords = insightsDao.getRecordsForSession(sessionId).map { it.toDomain() }
        )
    }

    override fun observeRecommendations(sessionId: Long): Flow<List<ProgressionRecommendation>> =
        insightsDao.observeRecommendationsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override fun observeRecords(sessionId: Long): Flow<List<PersonalRecord>> =
        insightsDao.observeRecordsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override fun observeLatestRecords(limit: Int): Flow<List<PersonalRecord>> =
        insightsDao.observeLatestRecords(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun saveCompletedWorkoutInsights(sessionId: Long) {
        val session = sessionDao.getSession(sessionId)?.toDomain() ?: return
        if (!session.isCompleted) return
        val recommendations = session.exercises.mapNotNull { exercise ->
            if (exercise.exerciseType != ExerciseType.STRENGTH) return@mapNotNull null
            val completedSets = exercise.sets.count { it.completed && !it.skipped }
            val targetSets = exercise.targetSets.coerceAtLeast(1)
            val targetReps = exercise.targetReps ?: 0
            val actualRepSum = exercise.sets.filter { it.completed && !it.skipped }.sumOf { it.actualReps ?: 0 }
            val targetRepSum = targetSets * targetReps
            val completionRatio = when {
                targetRepSum <= 0 -> completedSets.toDouble() / targetSets
                else -> actualRepSum.toDouble() / targetRepSum
            }
            val currentWeight = exercise.sets.mapNotNull { it.actualWeight ?: it.targetWeight }.maxOrNull() ?: exercise.targetWeight
            val action = when {
                completedSets == targetSets && exercise.sets.none { it.skipped } && exercise.sets.all { (it.actualReps ?: 0) >= (it.targetReps ?: 0) } -> RecommendationAction.INCREASE
                completionRatio >= 0.85 -> RecommendationAction.KEEP
                else -> RecommendationAction.DECREASE
            }
            val suggestedWeight = when (action) {
                RecommendationAction.INCREASE -> (currentWeight ?: 0.0) + 2.5
                RecommendationAction.KEEP -> currentWeight
                RecommendationAction.DECREASE -> currentWeight?.let { (it - 2.5).coerceAtLeast(0.0) }
                RecommendationAction.NONE -> null
            }
            ProgressionRecommendation(
                sessionId = session.id,
                exerciseId = exercise.exerciseId,
                exerciseName = exercise.exerciseName,
                action = action,
                currentWeight = currentWeight,
                suggestedWeight = suggestedWeight,
                message = recommendationMessage(exercise.exerciseName, action, suggestedWeight)
            )
        }

        val records = mutableListOf<PersonalRecord>()
        session.exercises.filter { !it.skipped }.forEach { exercise ->
            val completedSets = exercise.sets.filter { it.completed && !it.skipped }
            if (completedSets.isEmpty()) return@forEach
            val maxWeight = completedSets.maxOfOrNull { it.actualWeight ?: 0.0 } ?: 0.0
            val maxReps = completedSets.maxOfOrNull { (it.actualReps ?: 0).toDouble() } ?: 0.0
            val exerciseVolume = completedSets.sumOf { (it.actualWeight ?: 0.0) * (it.actualReps ?: 0) }
            val exerciseId = exercise.exerciseId ?: return@forEach
            val weightRecord = insightsDao.getTopExerciseRecord(exerciseId, PersonalRecordType.MAX_WEIGHT)
            if (maxWeight > (weightRecord?.value ?: 0.0)) {
                records += PersonalRecord(sessionId = session.id, exerciseId = exerciseId, exerciseName = exercise.exerciseName, recordType = PersonalRecordType.MAX_WEIGHT, value = maxWeight, achievedAt = session.completedAt ?: session.startedAt)
            }
            val repsRecord = insightsDao.getTopExerciseRecord(exerciseId, PersonalRecordType.MAX_REPS)
            if (maxReps > (repsRecord?.value ?: 0.0)) {
                records += PersonalRecord(sessionId = session.id, exerciseId = exerciseId, exerciseName = exercise.exerciseName, recordType = PersonalRecordType.MAX_REPS, value = maxReps, achievedAt = session.completedAt ?: session.startedAt)
            }
            val volumeRecord = insightsDao.getTopExerciseRecord(exerciseId, PersonalRecordType.MAX_EXERCISE_VOLUME)
            if (exerciseVolume > (volumeRecord?.value ?: 0.0)) {
                records += PersonalRecord(sessionId = session.id, exerciseId = exerciseId, exerciseName = exercise.exerciseName, recordType = PersonalRecordType.MAX_EXERCISE_VOLUME, value = exerciseVolume, achievedAt = session.completedAt ?: session.startedAt)
            }
        }
        val workoutVolume = session.strengthVolume()
        val topWorkout = insightsDao.getTopWorkoutVolumeRecord()
        if (workoutVolume > (topWorkout?.value ?: 0.0)) {
            records += PersonalRecord(sessionId = session.id, exerciseId = null, exerciseName = session.dayLabel, recordType = PersonalRecordType.MAX_WORKOUT_VOLUME, value = workoutVolume, achievedAt = session.completedAt ?: session.startedAt)
        }

        if (recommendations.isNotEmpty()) insightsDao.insertRecommendations(recommendations.map { it.toEntity() })
        if (records.isNotEmpty()) insightsDao.insertRecords(records.map { it.toEntity() })
    }

    override suspend fun getLatestRecommendationForExercise(exerciseId: Long): ProgressionRecommendation? =
        insightsDao.getLatestRecommendationForExercise(exerciseId)?.toDomain()
}

private fun compareLabel(current: Double, previous: Double?): String = when {
    previous == null || previous == 0.0 -> "Без сравнения"
    current > previous -> "Лучше прошлого результата"
    current < previous -> "Хуже прошлого результата"
    else -> "На уровне прошлого результата"
}

private fun compareMessage(current: Double, previous: Double?): String = when {
    previous == null || previous == 0.0 -> "Это первая завершенная тренировка такого типа"
    current > previous -> "Лучше прошлого результата"
    current < previous -> "Хуже прошлого результата"
    else -> "На уровне прошлого результата"
}

private fun recommendationMessage(name: String, action: RecommendationAction, suggestedWeight: Double?): String = when (action) {
    RecommendationAction.INCREASE -> "$name - увеличить до ${suggestedWeight ?: 0.0} кг"
    RecommendationAction.KEEP -> "$name - оставить текущий вес"
    RecommendationAction.DECREASE -> "$name - снизить до ${suggestedWeight ?: 0.0} кг"
    RecommendationAction.NONE -> "$name - без рекомендации"
}
