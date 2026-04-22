package com.example.fitnesapp.data.repository

import androidx.room.withTransaction
import com.example.fitnesapp.data.toDomain
import com.example.fitnesapp.data.toEntity
import com.example.fitnesapp.data.local.AppDatabase
import com.example.fitnesapp.data.local.dao.ExerciseDao
import com.example.fitnesapp.data.local.dao.BodyMeasurementDao
import com.example.fitnesapp.data.local.dao.NoteDao
import com.example.fitnesapp.data.local.dao.ProfileDao
import com.example.fitnesapp.data.local.dao.SessionDao
import com.example.fitnesapp.data.local.dao.WorkoutPlanDao
import com.example.fitnesapp.data.local.entity.SessionExerciseEntity
import com.example.fitnesapp.data.local.entity.ExerciseEntity
import com.example.fitnesapp.data.local.entity.WorkoutPlanDayEntity
import com.example.fitnesapp.data.local.entity.WorkoutSessionEntity
import com.example.fitnesapp.data.local.entity.WorkoutSetEntity
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.GoalFocusedMetrics
import com.example.fitnesapp.domain.model.MuscleGroup
import com.example.fitnesapp.domain.model.MuscleGroupStats
import com.example.fitnesapp.domain.model.Note
import com.example.fitnesapp.domain.model.ProgressPoint
import com.example.fitnesapp.domain.model.ProgressReport
import com.example.fitnesapp.domain.model.ProgressSummary
import com.example.fitnesapp.domain.model.ProgramTemplate
import com.example.fitnesapp.domain.model.UserProfile
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.WorkoutComparison
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutPlanExercise
import com.example.fitnesapp.domain.model.WorkoutStage
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.model.dayLabel
import com.example.fitnesapp.domain.model.label
import com.example.fitnesapp.domain.model.toLocalDate
import com.example.fitnesapp.domain.model.toShortDate
import com.example.fitnesapp.domain.repository.DemoRepository
import com.example.fitnesapp.domain.repository.AnalyticsRepository
import com.example.fitnesapp.domain.repository.BodyMeasurementRepository
import com.example.fitnesapp.domain.repository.ExerciseRepository
import com.example.fitnesapp.domain.repository.NoteRepository
import com.example.fitnesapp.domain.repository.ProfileRepository
import com.example.fitnesapp.domain.repository.ProgramTemplateRepository
import com.example.fitnesapp.domain.repository.ProgressRepository
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao
) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = profileDao.observeProfile().map { it?.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) {
        profileDao.upsert(profile.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }
}

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {
    override fun observeExercises(includeArchived: Boolean): Flow<List<Exercise>> =
        exerciseDao.observeExercises(includeArchived).map { entities ->
            entities.map { entity ->
                val used = exerciseDao.countPlanUsage(entity.id) > 0
                entity.toDomain(isUsedInPlan = used)
            }
        }

    override suspend fun getExercise(id: Long): Exercise? = exerciseDao.getById(id)?.toDomain(
        isUsedInPlan = exerciseDao.countPlanUsage(id) > 0
    )

    override suspend fun ensureExercise(exercise: Exercise): Exercise {
        val existing = exerciseDao.getByName(exercise.name.trim())
        if (existing != null) {
            return existing.toDomain(isUsedInPlan = exerciseDao.countPlanUsage(existing.id) > 0)
        }
        val now = System.currentTimeMillis()
        val id = exerciseDao.upsert(exercise.copy(isSystem = true).toEntity(now))
        return requireNotNull(exerciseDao.getById(id)) { "Failed to create exercise ${exercise.name}" }
            .toDomain(isUsedInPlan = exerciseDao.countPlanUsage(id) > 0)
    }

    override suspend fun saveExercise(exercise: Exercise) {
        exerciseDao.upsert(exercise.toEntity(System.currentTimeMillis()))
    }

    override suspend fun deleteExercise(id: Long): Boolean {
        val used = exerciseDao.countPlanUsage(id) > 0
        if (used) {
            exerciseDao.archive(id, System.currentTimeMillis())
            return false
        }
        exerciseDao.delete(id)
        return true
    }
}

class WorkoutPlanRepositoryImpl(
    private val planDao: WorkoutPlanDao,
    private val exerciseDao: ExerciseDao
) : WorkoutPlanRepository {
    override fun observePlan(): Flow<List<WorkoutPlanDay>> = combine(
        planDao.observePlanDays(),
        planDao.observeResolvedExercises()
    ) { days, resolved ->
        val dayMap = days.associateBy { it.day.dayOfWeek }
        (1..7).map { day ->
            dayMap[day]?.toDomain(resolved) ?: WorkoutPlanDay(day, true, emptyList(), day.dayLabel())
        }
    }

    override suspend fun getDay(dayOfWeek: Int): WorkoutPlanDay? {
        val day = planDao.getDay(dayOfWeek) ?: return null
        return day.toDomain(planDao.getResolvedExercisesForDay(dayOfWeek))
    }

    override suspend fun saveDay(day: WorkoutPlanDay) {
        planDao.upsertDay(WorkoutPlanDayEntity(day.dayOfWeek, day.isRestDay, day.title))
        planDao.deleteExercisesForDay(day.dayOfWeek)
        if (!day.isRestDay) {
            val safeExercises = day.exercises
                .sortedBy { it.orderInDay }
                .mapIndexed { index, item -> ensureExerciseExists(item).copy(orderInDay = index + 1, dayOfWeek = day.dayOfWeek) }
            planDao.insertExercises(safeExercises.map { it.toEntity() })
        }
    }

    override suspend fun applyTemplate(days: List<WorkoutPlanDay>) {
        (1..7).forEach { day ->
            saveDay(days.firstOrNull { it.dayOfWeek == day } ?: WorkoutPlanDay(day, true, emptyList(), day.dayLabel()))
        }
    }

    override suspend fun copyWorkoutDay(sourceDayOfWeek: Int, targetDayOfWeek: Int, overwrite: Boolean) {
        val source = getDay(sourceDayOfWeek) ?: return
        if (!overwrite) {
            val target = getDay(targetDayOfWeek)
            if (target != null && !target.isRestDay && target.exercises.isNotEmpty()) return
        }
        saveDay(source.copy(dayOfWeek = targetDayOfWeek, title = targetDayOfWeek.dayLabel(), exercises = source.exercises.mapIndexed { index, it -> it.copy(id = 0L, dayOfWeek = targetDayOfWeek, orderInDay = index + 1) }))
    }

    override suspend fun duplicateWorkoutExercise(dayOfWeek: Int, exerciseOrder: Int) {
        val day = getDay(dayOfWeek) ?: return
        val items = day.exercises.sortedBy { it.orderInDay }.toMutableList()
        val index = items.indexOfFirst { it.orderInDay == exerciseOrder }
        if (index == -1) return
        val original = items[index]
        items.add(index + 1, original.copy(id = 0L))
        saveDay(day.copy(exercises = items.mapIndexed { idx, item -> item.copy(orderInDay = idx + 1) }))
    }

    private suspend fun ensureExerciseExists(item: WorkoutPlanExercise): WorkoutPlanExercise {
        val existingById = item.exerciseId.takeIf { it > 0 }?.let { exerciseDao.getById(it) }
        if (existingById != null) {
            return item.copy(
                exerciseId = existingById.id,
                exerciseName = existingById.name,
                muscleGroupTag = existingById.muscleGroupTag,
                exerciseType = existingById.type,
                loadUnit = existingById.loadUnit
            )
        }

        val existingByName = exerciseDao.getByName(item.exerciseName)
        if (existingByName != null) {
            return item.copy(
                exerciseId = existingByName.id,
                exerciseName = existingByName.name,
                muscleGroupTag = existingByName.muscleGroupTag,
                exerciseType = existingByName.type,
                loadUnit = existingByName.loadUnit
            )
        }

        val now = System.currentTimeMillis()
        val createdId = exerciseDao.upsert(
            ExerciseEntity(
                name = item.exerciseName,
                muscleGroup = item.muscleGroupTag.label(),
                muscleGroupTag = item.muscleGroupTag,
                type = item.exerciseType,
                description = "Автоматически создано из тренировочного плана",
                loadUnit = item.loadUnit,
                createdAt = now,
                updatedAt = now,
                isSystem = true,
                isArchived = false
            )
        )
        val created = requireNotNull(exerciseDao.getById(createdId)) { "Failed to create exercise for plan: ${item.exerciseName}" }
        return item.copy(
            exerciseId = created.id,
            exerciseName = created.name,
            muscleGroupTag = created.muscleGroupTag,
            exerciseType = created.type,
            loadUnit = created.loadUnit
        )
    }
}

class SessionRepositoryImpl(
    private val database: AppDatabase,
    private val sessionDao: SessionDao
) : SessionRepository {
    override fun observeActiveSession(): Flow<WorkoutSession?> = sessionDao.observeActiveSession().map { it?.toDomain() }

    override fun observeSession(id: Long): Flow<WorkoutSession?> = sessionDao.observeSession(id).map { it?.toDomain() }

    override fun observeHistory(): Flow<List<WorkoutSession>> = sessionDao.observeHistory().map { it.map { item -> item.toDomain() } }

    override suspend fun getSession(id: Long): WorkoutSession? = sessionDao.getSession(id)?.toDomain()

    override suspend fun getActiveSession(): WorkoutSession? = sessionDao.getActiveSession()?.toDomain()

    override suspend fun startSession(day: WorkoutPlanDay): Long = database.withTransaction {
        val now = System.currentTimeMillis()
        val sessionId = sessionDao.insertSession(
            WorkoutSessionEntity(
                dayOfWeek = day.dayOfWeek,
                dayLabel = day.title,
                startedAt = now,
                completedAt = null,
                isCompleted = false,
                currentExerciseIndex = 0,
                currentStage = WorkoutStage.INPUT_SET,
                restTimerEndAt = null
            )
        )
        val exerciseIds = sessionDao.insertSessionExercises(
            day.exercises.sortedBy { it.orderInDay }.map {
                SessionExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = it.exerciseId,
                    plannedExerciseId = it.exerciseId,
                    plannedExerciseName = it.exerciseName,
                    exerciseName = it.exerciseName,
                    muscleGroupTag = it.muscleGroupTag,
                    exerciseType = it.exerciseType,
                    loadUnit = it.loadUnit,
                    orderInWorkout = it.orderInDay,
                    targetSets = it.targetSets,
                    targetReps = it.targetReps,
                    targetWeight = it.targetWeight,
                    restSeconds = it.restSeconds,
                    note = it.note
                )
            }
        )
        val sets = mutableListOf<WorkoutSetEntity>()
        day.exercises.sortedBy { it.orderInDay }.forEachIndexed { index, exercise ->
            repeat(exercise.targetSets) { setIndex ->
                sets += WorkoutSetEntity(
                    sessionExerciseId = exerciseIds[index],
                    orderInExercise = setIndex + 1,
                    targetReps = exercise.targetReps,
                    targetWeight = exercise.targetWeight,
                    actualReps = null,
                    actualWeight = null,
                    updatedAt = now
                )
            }
        }
        sessionDao.insertSets(sets)
        sessionId
    }

    override suspend fun updateSet(setId: Long, actualReps: Int?, actualWeight: Double?, completed: Boolean, skipped: Boolean) {
        sessionDao.updateSet(setId, actualReps, actualWeight, completed, skipped, System.currentTimeMillis())
    }

    override suspend fun markExerciseSkipped(sessionExerciseId: Long, skipped: Boolean) {
        sessionDao.markExerciseSkipped(sessionExerciseId, skipped)
    }

    override suspend fun setCurrentExercise(sessionId: Long, exerciseIndex: Int) {
        sessionDao.setCurrentExercise(sessionId, exerciseIndex)
    }

    override suspend fun advanceToNextExercise(sessionId: Long, exerciseIndex: Int) {
        sessionDao.advanceToNextExercise(sessionId, exerciseIndex)
    }

    override suspend fun setStage(sessionId: Long, stage: WorkoutStage) {
        sessionDao.setStage(sessionId, stage)
    }

    override suspend fun setRestTimer(sessionId: Long, restTimerEndAt: Long?) {
        sessionDao.setRestTimer(sessionId, restTimerEndAt)
    }

    override suspend fun cancelActiveSession(sessionId: Long) {
        sessionDao.deleteActiveSession(sessionId)
    }

    override suspend fun replaceCurrentExercise(sessionId: Long, sessionExerciseId: Long, replacement: Exercise) {
        sessionDao.replaceSessionExercise(
            sessionExerciseId = sessionExerciseId,
            exerciseId = replacement.id,
            exerciseName = replacement.name,
            muscleGroupTag = replacement.muscleGroupTag,
            exerciseType = replacement.type,
            loadUnit = replacement.loadUnit
        )
    }

    override suspend fun completeSession(sessionId: Long): WorkoutComparison {
        val current = sessionDao.getSession(sessionId)?.toDomain() ?: return WorkoutComparison(0.0, null, "Тренировка не найдена")
        sessionDao.completeSession(sessionId, System.currentTimeMillis())
        val sessions = sessionDao.getLastCompletedSessionsForDay(current.dayOfWeek).map { it.toDomain() }
        val currentVolume = current.totalStrengthVolume()
        val previous = sessions.firstOrNull { it.id != sessionId }
        val previousVolume = previous?.totalStrengthVolume()
        val message = comparisonMessage(currentVolume, previousVolume)
        return WorkoutComparison(currentVolume, previousVolume, message)
    }
}

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun observeNotes(): Flow<List<Note>> = noteDao.observeNotes().map { it.map { entity -> entity.toDomain() } }

    override suspend fun saveNote(note: Note) {
        noteDao.upsert(note.toEntity(System.currentTimeMillis()))
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.delete(id)
    }
}

class ProgressRepositoryImpl(
    private val sessionRepository: SessionRepository
) : ProgressRepository {
    override fun observeProgress(exerciseId: Long?, periodDays: Int?): Flow<ProgressReport> = sessionRepository.observeHistory().map { sessions ->
        val minDate = periodDays?.let { LocalDate.now().minusDays(it.toLong()) }
        val filteredSessions = sessions.filter { session ->
            val afterPeriod = minDate == null || session.startedAt.toLocalDate().isAfter(minDate.minusDays(1))
            val containsExercise = exerciseId == null || session.exercises.any { it.exerciseId == exerciseId }
            afterPeriod && containsExercise
        }
        val points = filteredSessions.mapNotNull { session ->
            val exercises = session.exercises.filter { it.exerciseType == ExerciseType.STRENGTH && (exerciseId == null || it.exerciseId == exerciseId) }
            if (exercises.isEmpty()) return@mapNotNull null
            val sets = exercises.flatMap { it.sets }.filter { it.completed && !it.skipped }
            val volume = sets.sumOf { (it.actualWeight ?: 0.0) * (it.actualReps ?: 0) }
            val avgWeight = if (sets.isEmpty()) 0.0 else sets.mapNotNull { it.actualWeight }.average()
            val avgReps = if (sets.isEmpty()) 0 else sets.mapNotNull { it.actualReps }.average().toInt()
            ProgressPoint(session.startedAt, session.startedAt.toShortDate(), avgWeight, avgReps, volume)
        }.sortedBy { it.timestamp }
        val summary = ProgressSummary(
            totalWorkouts = filteredSessions.size,
            totalVolume = points.sumOf { it.volume },
            bestVolume = points.maxOfOrNull { it.volume } ?: 0.0,
            averageVolume = points.map { it.volume }.average().takeUnless { it.isNaN() } ?: 0.0
        )
        ProgressReport(points = points, history = filteredSessions, summary = summary)
    }
}

class DemoRepositoryImpl(
    private val database: AppDatabase,
    private val profileDao: ProfileDao,
    private val exerciseDao: ExerciseDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val sessionDao: SessionDao,
    private val noteDao: NoteDao,
    private val sessionRepository: SessionRepository
) : DemoRepository {
    override suspend fun seedIfNeeded() {
        // Demo data disabled intentionally.
    }

    private suspend fun createDemoHistory() {
        return
    }

    override suspend fun clearAllUserData() {
        database.withTransaction {
            sessionDao.clearSets()
            sessionDao.clearSessionExercises()
            sessionDao.clearSessions()
            workoutPlanDao.clearExercises()
            workoutPlanDao.clearDays()
            exerciseDao.clear()
            noteDao.clear()
            profileDao.clear()
        }
        seedIfNeeded()
    }
}

private fun WorkoutSession.totalStrengthVolume(): Double = exercises
    .filter { it.exerciseType == ExerciseType.STRENGTH && !it.skipped }
    .flatMap { it.sets }
    .filter { it.completed && !it.skipped }
    .sumOf { (it.actualWeight ?: 0.0) * (it.actualReps ?: 0) }

private fun comparisonMessage(currentVolume: Double, previousVolume: Double?): String {
    if (previousVolume == null || previousVolume == 0.0) {
        return "Это ваша первая зафиксированная тренировка этого типа"
    }
    return when {
        currentVolume > previousVolume -> {
            val delta = ((currentVolume - previousVolume) / previousVolume) * 100.0
            "Сегодня вы молодец, вы превзошли свой прошлый результат на ${"%.1f".format(delta)}%"
        }
        currentVolume < previousVolume -> {
            val delta = ((previousVolume - currentVolume) / previousVolume) * 100.0
            "Сегодня вы, возможно, устали - в прошлый раз вы были сильнее на ${"%.1f".format(delta)}%"
        }
        else -> "Сегодня результат на уровне прошлой тренировки"
    }
}
