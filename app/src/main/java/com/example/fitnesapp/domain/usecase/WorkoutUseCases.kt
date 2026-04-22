package com.example.fitnesapp.domain.usecase

import com.example.fitnesapp.domain.model.TodayWorkoutDecision
import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.GoalFocusedMetrics
import com.example.fitnesapp.domain.model.MuscleGroupStats
import com.example.fitnesapp.domain.model.PersonalRecord
import com.example.fitnesapp.domain.model.ProgressionRecommendation
import com.example.fitnesapp.domain.model.ProgramTemplate
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.WorkoutComparison
import com.example.fitnesapp.domain.model.WorkoutHistoryDetails
import com.example.fitnesapp.domain.model.WorkoutHistoryItem
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.repository.AnalyticsRepository
import com.example.fitnesapp.domain.repository.BodyMeasurementRepository
import com.example.fitnesapp.domain.repository.DemoRepository
import com.example.fitnesapp.domain.repository.MaintenanceRepository
import com.example.fitnesapp.domain.repository.ProgramTemplateRepository
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.repository.WorkoutInsightsRepository
import com.example.fitnesapp.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class SeedDemoDataUseCase(
    private val demoRepository: DemoRepository
) {
    suspend operator fun invoke() = demoRepository.seedIfNeeded()
}

class ResetDataUseCase(
    private val demoRepository: DemoRepository
) {
    suspend operator fun invoke() = demoRepository.clearAllUserData()
}

class GetTodayWorkoutUseCase(
    private val sessionRepository: SessionRepository,
    private val workoutPlanRepository: WorkoutPlanRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): TodayWorkoutDecision {
        val active = sessionRepository.getActiveSession()
        if (active != null) {
            return TodayWorkoutDecision(
                activeSessionId = active.id,
                message = "У вас есть незавершенная тренировка"
            )
        }
        val day = workoutPlanRepository.getDay(date.dayOfWeek.value)
        return when {
            day == null || day.isRestDay || day.exercises.isEmpty() -> {
                TodayWorkoutDecision(message = "Сегодня вы отдыхаете")
            }
            else -> TodayWorkoutDecision(plannedDay = day, message = "На сегодня запланирована тренировка")
        }
    }
}

class StartWorkoutUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(day: WorkoutPlanDay): Long = sessionRepository.startSession(day)
}

class CompleteWorkoutUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long): WorkoutComparison = sessionRepository.completeSession(sessionId)
}

class GetWorkoutSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long): WorkoutSession? = sessionRepository.getSession(sessionId)
}

class SaveCompletedWorkoutUseCase(
    private val repository: WorkoutInsightsRepository
) {
    suspend operator fun invoke(sessionId: Long) = repository.saveCompletedWorkoutInsights(sessionId)
}

class GetWorkoutHistoryListUseCase(
    private val repository: WorkoutInsightsRepository
) {
    operator fun invoke(): Flow<List<WorkoutHistoryItem>> = repository.observeWorkoutHistory()
}

class GetWorkoutHistoryDetailsUseCase(
    private val repository: WorkoutInsightsRepository
) {
    suspend operator fun invoke(sessionId: Long): WorkoutHistoryDetails? = repository.getWorkoutHistoryDetails(sessionId)
}

class GetWorkoutRecommendationsUseCase(
    private val repository: WorkoutInsightsRepository
) {
    operator fun invoke(sessionId: Long): Flow<List<ProgressionRecommendation>> = repository.observeRecommendations(sessionId)
}

class GetWorkoutRecordsUseCase(
    private val repository: WorkoutInsightsRepository
) {
    operator fun invoke(sessionId: Long): Flow<List<PersonalRecord>> = repository.observeRecords(sessionId)
}

class GetLatestPersonalRecordsUseCase(
    private val repository: WorkoutInsightsRepository
) {
    operator fun invoke(limit: Int = 5): Flow<List<PersonalRecord>> = repository.observeLatestRecords(limit)
}

class GetMuscleGroupStatsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(periodDays: Int?): Flow<List<MuscleGroupStats>> = repository.observeMuscleGroupStats(periodDays)
}

class GetGoalFocusedMetricsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(periodDays: Int?): Flow<List<GoalFocusedMetrics>> = repository.observeGoalFocusedMetrics(periodDays)
}

class GetProgramTemplatesUseCase(private val repository: ProgramTemplateRepository) {
    operator fun invoke(): List<ProgramTemplate> = repository.getTemplates()
}

class AddBodyMeasurementUseCase(private val repository: BodyMeasurementRepository) {
    suspend operator fun invoke(item: BodyMeasurement) = repository.save(item)
}

class UpdateBodyMeasurementUseCase(private val repository: BodyMeasurementRepository) {
    suspend operator fun invoke(item: BodyMeasurement) = repository.save(item)
}

class DeleteBodyMeasurementUseCase(private val repository: BodyMeasurementRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}

class GetBodyMeasurementsHistoryUseCase(private val repository: BodyMeasurementRepository) {
    operator fun invoke(): Flow<List<BodyMeasurement>> = repository.observeHistory()
}

class GetLatestBodyMeasurementsUseCase(private val repository: BodyMeasurementRepository) {
    operator fun invoke(): Flow<BodyMeasurement?> = repository.observeLatest()
}

class UpdateUserGoalUseCase(private val repository: AnalyticsRepository) {
    suspend operator fun invoke(goal: UserGoal, note: String) = repository.updateUserGoal(goal, note)
}

class ClearProgressDataUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke() = repository.clearProgressData()
}

class ClearWorkoutHistoryUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke() = repository.clearWorkoutHistory()
}
