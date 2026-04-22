package com.example.fitnesapp.data

import com.example.fitnesapp.data.local.entity.BodyMeasurementEntity
import com.example.fitnesapp.data.local.entity.ExerciseEntity
import com.example.fitnesapp.data.local.entity.NoteEntity
import com.example.fitnesapp.data.local.entity.PersonalRecordEntity
import com.example.fitnesapp.data.local.entity.ProfileEntity
import com.example.fitnesapp.data.local.entity.ProgressionRecommendationEntity
import com.example.fitnesapp.data.local.entity.SessionExerciseWithSets
import com.example.fitnesapp.data.local.entity.WorkoutPlanDayWithExercises
import com.example.fitnesapp.data.local.entity.WorkoutPlanExerciseEntity
import com.example.fitnesapp.data.local.entity.WorkoutPlanExerciseResolved
import com.example.fitnesapp.data.local.entity.WorkoutSessionWithDetails
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.Note
import com.example.fitnesapp.domain.model.PersonalRecord
import com.example.fitnesapp.domain.model.ProgressionRecommendation
import com.example.fitnesapp.domain.model.UserProfile
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutPlanExercise
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.model.WorkoutSessionExercise
import com.example.fitnesapp.domain.model.WorkoutSet

fun ProfileEntity.toDomain() = UserProfile(name, gender, age, heightCm, weightKg, goal, goalNote, updatedAt)

fun UserProfile.toEntity() = ProfileEntity(
    name = name,
    gender = gender,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    goal = goal,
    goalNote = goalNote,
    updatedAt = updatedAt
)

fun ExerciseEntity.toDomain(isUsedInPlan: Boolean = false) = Exercise(
    id = id,
    name = name,
    muscleGroup = muscleGroup,
    muscleGroupTag = muscleGroupTag,
    type = type,
    description = description,
    loadUnit = loadUnit,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSystem = isSystem,
    isArchived = isArchived,
    isUsedInPlan = isUsedInPlan
)

fun Exercise.toEntity(now: Long) = ExerciseEntity(
    id = id,
    name = name,
    muscleGroup = muscleGroup,
    muscleGroupTag = muscleGroupTag,
    type = type,
    description = description,
    loadUnit = loadUnit,
    createdAt = if (createdAt == 0L) now else createdAt,
    updatedAt = now,
    isSystem = isSystem,
    isArchived = isArchived
)

fun WorkoutPlanExerciseResolved.toDomain() = WorkoutPlanExercise(
    id = id,
    dayOfWeek = dayOfWeek,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    muscleGroupTag = muscleGroupTag,
    exerciseType = exerciseType,
    loadUnit = loadUnit,
    orderInDay = orderInDay,
    targetSets = targetSets,
    targetReps = targetReps,
    targetWeight = targetWeight,
    restSeconds = restSeconds,
    note = note
)

fun WorkoutPlanDayWithExercises.toDomain(resolved: List<WorkoutPlanExerciseResolved>) = WorkoutPlanDay(
    dayOfWeek = day.dayOfWeek,
    isRestDay = day.isRestDay,
    exercises = resolved.filter { it.dayOfWeek == day.dayOfWeek }.sortedBy { it.orderInDay }.map { it.toDomain() },
    title = day.title
)

fun WorkoutPlanExercise.toEntity() = WorkoutPlanExerciseEntity(
    id = id,
    dayOfWeek = dayOfWeek,
    exerciseId = exerciseId,
    orderInDay = orderInDay,
    targetSets = targetSets,
    targetReps = targetReps,
    targetWeight = targetWeight,
    restSeconds = restSeconds,
    note = note
)

fun SessionExerciseWithSets.toDomain() = WorkoutSessionExercise(
    id = exercise.id,
    sessionId = exercise.sessionId,
    exerciseId = exercise.exerciseId,
    plannedExerciseId = exercise.plannedExerciseId,
    plannedExerciseName = exercise.plannedExerciseName,
    exerciseName = exercise.exerciseName,
    muscleGroupTag = exercise.muscleGroupTag,
    exerciseType = exercise.exerciseType,
    loadUnit = exercise.loadUnit,
    orderInWorkout = exercise.orderInWorkout,
    targetSets = exercise.targetSets,
    targetReps = exercise.targetReps,
    targetWeight = exercise.targetWeight,
    restSeconds = exercise.restSeconds,
    note = exercise.note,
    isReplacement = exercise.isReplacement,
    skipped = exercise.skipped,
    sets = sets.sortedBy { it.orderInExercise }.map {
        WorkoutSet(
            id = it.id,
            sessionExerciseId = it.sessionExerciseId,
            orderInExercise = it.orderInExercise,
            targetReps = it.targetReps,
            targetWeight = it.targetWeight,
            actualReps = it.actualReps,
            actualWeight = it.actualWeight,
            completed = it.completed,
            skipped = it.skipped,
            updatedAt = it.updatedAt
        )
    }
)

fun WorkoutSessionWithDetails.toDomain() = WorkoutSession(
    id = session.id,
    dayOfWeek = session.dayOfWeek,
    dayLabel = session.dayLabel,
    startedAt = session.startedAt,
    completedAt = session.completedAt,
    isCompleted = session.isCompleted,
    currentExerciseIndex = session.currentExerciseIndex,
    currentStage = session.currentStage,
    restTimerEndAt = session.restTimerEndAt,
    exercises = exercises.sortedBy { it.exercise.orderInWorkout }.map { it.toDomain() }
)

fun NoteEntity.toDomain() = Note(id, title, text, workoutDate, createdAt, updatedAt)

fun Note.toEntity(now: Long) = NoteEntity(
    id = id,
    title = title,
    text = text,
    workoutDate = workoutDate,
    createdAt = if (createdAt == 0L) now else createdAt,
    updatedAt = now
)

fun ProgressionRecommendationEntity.toDomain() = ProgressionRecommendation(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    action = action,
    currentWeight = currentWeight,
    suggestedWeight = suggestedWeight,
    message = message
)

fun ProgressionRecommendation.toEntity() = ProgressionRecommendationEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    action = action,
    currentWeight = currentWeight,
    suggestedWeight = suggestedWeight,
    message = message
)

fun PersonalRecordEntity.toDomain() = PersonalRecord(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    recordType = recordType,
    value = value,
    achievedAt = achievedAt
)

fun PersonalRecord.toEntity() = PersonalRecordEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    recordType = recordType,
    value = value,
    achievedAt = achievedAt
)

fun BodyMeasurementEntity.toDomain() = BodyMeasurement(
    id = id,
    measuredAt = measuredAt,
    bodyWeightKg = bodyWeightKg,
    neckCm = neckCm,
    shouldersCm = shouldersCm,
    chestCm = chestCm,
    waistCm = waistCm,
    bellyCm = bellyCm,
    hipsCm = hipsCm,
    glutesCm = glutesCm,
    bicepsCm = bicepsCm,
    forearmCm = forearmCm,
    thighCm = thighCm,
    calfCm = calfCm,
    note = note
)

fun BodyMeasurement.toEntity() = BodyMeasurementEntity(
    id = id,
    measuredAt = measuredAt,
    bodyWeightKg = bodyWeightKg,
    neckCm = neckCm,
    shouldersCm = shouldersCm,
    chestCm = chestCm,
    waistCm = waistCm,
    bellyCm = bellyCm,
    hipsCm = hipsCm,
    glutesCm = glutesCm,
    bicepsCm = bicepsCm,
    forearmCm = forearmCm,
    thighCm = thighCm,
    calfCm = calfCm,
    note = note
)
