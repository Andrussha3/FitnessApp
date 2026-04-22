package com.example.fitnesapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesapp.data.AppContainer
import com.example.fitnesapp.data.notification.RestTimerScheduler
import com.example.fitnesapp.presentation.screen.active.ActiveWorkoutViewModel
import com.example.fitnesapp.presentation.screen.exercise.ExercisesViewModel
import com.example.fitnesapp.presentation.screen.home.HomeViewModel
import com.example.fitnesapp.presentation.screen.history.WorkoutHistoryDetailsViewModel
import com.example.fitnesapp.presentation.screen.history.WorkoutHistoryViewModel
import com.example.fitnesapp.presentation.screen.notes.NotesViewModel
import com.example.fitnesapp.presentation.screen.progress.ProgressViewModel
import com.example.fitnesapp.presentation.screen.result.WorkoutResultViewModel
import com.example.fitnesapp.presentation.screen.settings.SettingsViewModel
import com.example.fitnesapp.presentation.screen.start.StartWorkoutViewModel
import com.example.fitnesapp.presentation.screen.workout.WorkoutPlanViewModel
import com.example.fitnesapp.presentation.theme.ThemeViewModel

class AppViewModelFactory(
    private val container: AppContainer,
    private val sessionId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(
                container.profileRepository,
                container.workoutPlanRepository,
                container.sessionRepository,
                container.seedDemoDataUseCase,
                container.getTodayWorkoutUseCase
            ) as T

            ExercisesViewModel::class.java -> ExercisesViewModel(container.exerciseRepository) as T
            WorkoutPlanViewModel::class.java -> WorkoutPlanViewModel(container.workoutPlanRepository, container.exerciseRepository, container.getProgramTemplatesUseCase) as T
            WorkoutHistoryViewModel::class.java -> WorkoutHistoryViewModel(container.getWorkoutHistoryListUseCase) as T
            WorkoutHistoryDetailsViewModel::class.java -> WorkoutHistoryDetailsViewModel(requireNotNull(sessionId), container.getWorkoutHistoryDetailsUseCase) as T
            NotesViewModel::class.java -> NotesViewModel(container.noteRepository) as T
            SettingsViewModel::class.java -> SettingsViewModel(container.profileRepository, container.bodyMeasurementRepository, container.themeRepository, container.clearProgressDataUseCase, container.clearWorkoutHistoryUseCase) as T
            ProgressViewModel::class.java -> ProgressViewModel(container.progressRepository, container.exerciseRepository, container.getMuscleGroupStatsUseCase, container.getGoalFocusedMetricsUseCase) as T
            ThemeViewModel::class.java -> ThemeViewModel(container.themeRepository) as T
            StartWorkoutViewModel::class.java -> StartWorkoutViewModel(
                container.getTodayWorkoutUseCase,
                container.startWorkoutUseCase,
                container.sessionRepository
            ) as T

            ActiveWorkoutViewModel::class.java -> ActiveWorkoutViewModel(
                sessionId = requireNotNull(sessionId),
                sessionRepository = container.sessionRepository,
                completeWorkoutUseCase = container.completeWorkoutUseCase,
                exerciseRepository = container.exerciseRepository,
                workoutInsightsRepository = container.workoutInsightsRepository,
                scheduler = container.restTimerScheduler
            ) as T

            WorkoutResultViewModel::class.java -> WorkoutResultViewModel(
                requireNotNull(sessionId),
                container.sessionRepository,
                container.saveCompletedWorkoutUseCase,
                container.getWorkoutRecommendationsUseCase,
                container.getWorkoutRecordsUseCase
            ) as T

            else -> error("Unknown ViewModel: ${modelClass.simpleName}")
        }
    }
}

fun restTimerScheduler(container: AppContainer): RestTimerScheduler = container.restTimerScheduler
