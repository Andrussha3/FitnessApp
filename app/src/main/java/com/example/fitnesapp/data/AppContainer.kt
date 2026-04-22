package com.example.fitnesapp.data

import android.content.Context
import androidx.room.Room
import com.example.fitnesapp.data.local.AppDatabase
import com.example.fitnesapp.data.notification.RestTimerScheduler
import com.example.fitnesapp.data.repository.DemoRepositoryImpl
import com.example.fitnesapp.data.repository.AnalyticsRepositoryImpl
import com.example.fitnesapp.data.repository.BodyMeasurementRepositoryImpl
import com.example.fitnesapp.data.repository.ExerciseRepositoryImpl
import com.example.fitnesapp.data.repository.MaintenanceRepositoryImpl
import com.example.fitnesapp.data.repository.NoteRepositoryImpl
import com.example.fitnesapp.data.repository.ProfileRepositoryImpl
import com.example.fitnesapp.data.repository.ProgressRepositoryImpl
import com.example.fitnesapp.data.repository.ProgramTemplateRepositoryImpl
import com.example.fitnesapp.data.repository.SessionRepositoryImpl
import com.example.fitnesapp.data.repository.ThemeRepositoryImpl
import com.example.fitnesapp.data.repository.WorkoutInsightsRepositoryImpl
import com.example.fitnesapp.data.repository.WorkoutPlanRepositoryImpl
import com.example.fitnesapp.domain.repository.AnalyticsRepository
import com.example.fitnesapp.domain.repository.BodyMeasurementRepository
import com.example.fitnesapp.domain.repository.DemoRepository
import com.example.fitnesapp.domain.repository.ExerciseRepository
import com.example.fitnesapp.domain.repository.NoteRepository
import com.example.fitnesapp.domain.repository.MaintenanceRepository
import com.example.fitnesapp.domain.repository.ProfileRepository
import com.example.fitnesapp.domain.repository.ProgressRepository
import com.example.fitnesapp.domain.repository.ProgramTemplateRepository
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.repository.ThemeRepository
import com.example.fitnesapp.domain.repository.WorkoutInsightsRepository
import com.example.fitnesapp.domain.repository.WorkoutPlanRepository
import com.example.fitnesapp.domain.usecase.AddBodyMeasurementUseCase
import com.example.fitnesapp.domain.usecase.CompleteWorkoutUseCase
import com.example.fitnesapp.domain.usecase.ClearProgressDataUseCase
import com.example.fitnesapp.domain.usecase.ClearWorkoutHistoryUseCase
import com.example.fitnesapp.domain.usecase.DeleteBodyMeasurementUseCase
import com.example.fitnesapp.domain.usecase.GetBodyMeasurementsHistoryUseCase
import com.example.fitnesapp.domain.usecase.GetGoalFocusedMetricsUseCase
import com.example.fitnesapp.domain.usecase.GetLatestBodyMeasurementsUseCase
import com.example.fitnesapp.domain.usecase.GetTodayWorkoutUseCase
import com.example.fitnesapp.domain.usecase.GetLatestPersonalRecordsUseCase
import com.example.fitnesapp.domain.usecase.GetMuscleGroupStatsUseCase
import com.example.fitnesapp.domain.usecase.GetProgramTemplatesUseCase
import com.example.fitnesapp.domain.usecase.GetWorkoutHistoryDetailsUseCase
import com.example.fitnesapp.domain.usecase.GetWorkoutHistoryListUseCase
import com.example.fitnesapp.domain.usecase.GetWorkoutSessionUseCase
import com.example.fitnesapp.domain.usecase.GetWorkoutRecordsUseCase
import com.example.fitnesapp.domain.usecase.GetWorkoutRecommendationsUseCase
import com.example.fitnesapp.domain.usecase.ResetDataUseCase
import com.example.fitnesapp.domain.usecase.SaveCompletedWorkoutUseCase
import com.example.fitnesapp.domain.usecase.SeedDemoDataUseCase
import com.example.fitnesapp.domain.usecase.StartWorkoutUseCase
import com.example.fitnesapp.domain.usecase.UpdateBodyMeasurementUseCase

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, "fitnes_app.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(database.profileDao()) }
    val exerciseRepository: ExerciseRepository by lazy { ExerciseRepositoryImpl(database.exerciseDao()) }
    val workoutPlanRepository: WorkoutPlanRepository by lazy { WorkoutPlanRepositoryImpl(database.workoutPlanDao(), database.exerciseDao()) }
    val sessionRepository: SessionRepository by lazy { SessionRepositoryImpl(database, database.sessionDao()) }
    val noteRepository: NoteRepository by lazy { NoteRepositoryImpl(database.noteDao()) }
    val progressRepository: ProgressRepository by lazy { ProgressRepositoryImpl(sessionRepository) }
    val bodyMeasurementRepository: BodyMeasurementRepository by lazy { BodyMeasurementRepositoryImpl(database.bodyMeasurementDao()) }
    val programTemplateRepository: ProgramTemplateRepository by lazy { ProgramTemplateRepositoryImpl() }
    val themeRepository: ThemeRepository by lazy { ThemeRepositoryImpl(appContext) }
    val workoutInsightsRepository: WorkoutInsightsRepository by lazy { WorkoutInsightsRepositoryImpl(database.sessionDao(), database.insightsDao()) }
    val analyticsRepository: AnalyticsRepository by lazy { AnalyticsRepositoryImpl(sessionRepository, profileRepository, bodyMeasurementRepository) }
    val maintenanceRepository: MaintenanceRepository by lazy { MaintenanceRepositoryImpl(database) }
    val demoRepository: DemoRepository by lazy {
        DemoRepositoryImpl(
            database = database,
            profileDao = database.profileDao(),
            exerciseDao = database.exerciseDao(),
            workoutPlanDao = database.workoutPlanDao(),
            sessionDao = database.sessionDao(),
            noteDao = database.noteDao(),
            sessionRepository = sessionRepository
        )
    }
    val restTimerScheduler: RestTimerScheduler by lazy { RestTimerScheduler(appContext) }

    val seedDemoDataUseCase by lazy { SeedDemoDataUseCase(demoRepository) }
    val resetDataUseCase by lazy { ResetDataUseCase(demoRepository) }
    val getTodayWorkoutUseCase by lazy { GetTodayWorkoutUseCase(sessionRepository, workoutPlanRepository) }
    val startWorkoutUseCase by lazy { StartWorkoutUseCase(sessionRepository) }
    val completeWorkoutUseCase by lazy { CompleteWorkoutUseCase(sessionRepository) }
    val getWorkoutSessionUseCase by lazy { GetWorkoutSessionUseCase(sessionRepository) }
    val saveCompletedWorkoutUseCase by lazy { SaveCompletedWorkoutUseCase(workoutInsightsRepository) }
    val getWorkoutHistoryListUseCase by lazy { GetWorkoutHistoryListUseCase(workoutInsightsRepository) }
    val getWorkoutHistoryDetailsUseCase by lazy { GetWorkoutHistoryDetailsUseCase(workoutInsightsRepository) }
    val getWorkoutRecommendationsUseCase by lazy { GetWorkoutRecommendationsUseCase(workoutInsightsRepository) }
    val getWorkoutRecordsUseCase by lazy { GetWorkoutRecordsUseCase(workoutInsightsRepository) }
    val getLatestPersonalRecordsUseCase by lazy { GetLatestPersonalRecordsUseCase(workoutInsightsRepository) }
    val addBodyMeasurementUseCase by lazy { AddBodyMeasurementUseCase(bodyMeasurementRepository) }
    val updateBodyMeasurementUseCase by lazy { UpdateBodyMeasurementUseCase(bodyMeasurementRepository) }
    val deleteBodyMeasurementUseCase by lazy { DeleteBodyMeasurementUseCase(bodyMeasurementRepository) }
    val getBodyMeasurementsHistoryUseCase by lazy { GetBodyMeasurementsHistoryUseCase(bodyMeasurementRepository) }
    val getLatestBodyMeasurementsUseCase by lazy { GetLatestBodyMeasurementsUseCase(bodyMeasurementRepository) }
    val getProgramTemplatesUseCase by lazy { GetProgramTemplatesUseCase(programTemplateRepository) }
    val getMuscleGroupStatsUseCase by lazy { GetMuscleGroupStatsUseCase(analyticsRepository) }
    val getGoalFocusedMetricsUseCase by lazy { GetGoalFocusedMetricsUseCase(analyticsRepository) }
    val clearProgressDataUseCase by lazy { ClearProgressDataUseCase(maintenanceRepository) }
    val clearWorkoutHistoryUseCase by lazy { ClearWorkoutHistoryUseCase(maintenanceRepository) }
}
