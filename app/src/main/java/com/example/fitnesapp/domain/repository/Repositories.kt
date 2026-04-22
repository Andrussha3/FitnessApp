package com.example.fitnesapp.domain.repository

import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.GoalFocusedMetrics
import com.example.fitnesapp.domain.model.MuscleGroupStats
import com.example.fitnesapp.domain.model.Note
import com.example.fitnesapp.domain.model.PersonalRecord
import com.example.fitnesapp.domain.model.ProgressReport
import com.example.fitnesapp.domain.model.ProgressionRecommendation
import com.example.fitnesapp.domain.model.ProgramTemplate
import com.example.fitnesapp.domain.model.AppThemeMode
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.UserProfile
import com.example.fitnesapp.domain.model.WorkoutComparison
import com.example.fitnesapp.domain.model.WorkoutHistoryDetails
import com.example.fitnesapp.domain.model.WorkoutHistoryItem
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutStage
import com.example.fitnesapp.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
}

interface ExerciseRepository {
    fun observeExercises(includeArchived: Boolean = false): Flow<List<Exercise>>
    suspend fun getExercise(id: Long): Exercise?
    suspend fun ensureExercise(exercise: Exercise): Exercise
    suspend fun saveExercise(exercise: Exercise)
    suspend fun deleteExercise(id: Long): Boolean
}

interface WorkoutPlanRepository {
    fun observePlan(): Flow<List<WorkoutPlanDay>>
    suspend fun getDay(dayOfWeek: Int): WorkoutPlanDay?
    suspend fun saveDay(day: WorkoutPlanDay)
    suspend fun applyTemplate(days: List<WorkoutPlanDay>)
    suspend fun copyWorkoutDay(sourceDayOfWeek: Int, targetDayOfWeek: Int, overwrite: Boolean)
    suspend fun duplicateWorkoutExercise(dayOfWeek: Int, exerciseOrder: Int)
}

interface SessionRepository {
    fun observeActiveSession(): Flow<WorkoutSession?>
    fun observeSession(id: Long): Flow<WorkoutSession?>
    fun observeHistory(): Flow<List<WorkoutSession>>
    suspend fun getSession(id: Long): WorkoutSession?
    suspend fun getActiveSession(): WorkoutSession?
    suspend fun startSession(day: WorkoutPlanDay): Long
    suspend fun updateSet(setId: Long, actualReps: Int?, actualWeight: Double?, completed: Boolean, skipped: Boolean)
    suspend fun markExerciseSkipped(sessionExerciseId: Long, skipped: Boolean)
    suspend fun setCurrentExercise(sessionId: Long, exerciseIndex: Int)
    suspend fun advanceToNextExercise(sessionId: Long, exerciseIndex: Int)
    suspend fun setStage(sessionId: Long, stage: WorkoutStage)
    suspend fun setRestTimer(sessionId: Long, restTimerEndAt: Long?)
    suspend fun cancelActiveSession(sessionId: Long)
    suspend fun replaceCurrentExercise(sessionId: Long, sessionExerciseId: Long, replacement: Exercise)
    suspend fun completeSession(sessionId: Long): WorkoutComparison
}

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: Long)
}

interface ProgressRepository {
    fun observeProgress(exerciseId: Long?, periodDays: Int?): Flow<ProgressReport>
}

interface DemoRepository {
    suspend fun seedIfNeeded()
    suspend fun clearAllUserData()
}

interface ThemeRepository {
    fun observeTheme(): Flow<AppThemeMode>
    suspend fun setTheme(theme: AppThemeMode)
}

interface BodyMeasurementRepository {
    fun observeHistory(): Flow<List<BodyMeasurement>>
    fun observeLatest(): Flow<BodyMeasurement?>
    suspend fun save(measurement: BodyMeasurement)
    suspend fun delete(id: Long)
}

interface WorkoutInsightsRepository {
    fun observeWorkoutHistory(): Flow<List<WorkoutHistoryItem>>
    suspend fun getWorkoutHistoryDetails(sessionId: Long): WorkoutHistoryDetails?
    fun observeRecommendations(sessionId: Long): Flow<List<ProgressionRecommendation>>
    fun observeRecords(sessionId: Long): Flow<List<PersonalRecord>>
    fun observeLatestRecords(limit: Int = 5): Flow<List<PersonalRecord>>
    suspend fun saveCompletedWorkoutInsights(sessionId: Long)
    suspend fun getLatestRecommendationForExercise(exerciseId: Long): ProgressionRecommendation?
}

interface ProgramTemplateRepository {
    fun getTemplates(): List<ProgramTemplate>
}

interface AnalyticsRepository {
    fun observeMuscleGroupStats(periodDays: Int?): Flow<List<MuscleGroupStats>>
    fun observeGoalFocusedMetrics(periodDays: Int?): Flow<List<GoalFocusedMetrics>>
    suspend fun updateUserGoal(goal: UserGoal, note: String)
}

interface MaintenanceRepository {
    suspend fun clearProgressData()
    suspend fun clearWorkoutHistory()
}
