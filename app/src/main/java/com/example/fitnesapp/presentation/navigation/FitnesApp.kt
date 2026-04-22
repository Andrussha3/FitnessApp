package com.example.fitnesapp.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnesapp.FitnesApplication
import com.example.fitnesapp.data.AppContainer
import com.example.fitnesapp.presentation.AppViewModelFactory
import com.example.fitnesapp.presentation.screen.active.ActiveWorkoutScreen
import com.example.fitnesapp.presentation.screen.active.ActiveWorkoutViewModel
import com.example.fitnesapp.presentation.screen.exercise.ExercisesScreen
import com.example.fitnesapp.presentation.screen.exercise.ExercisesViewModel
import com.example.fitnesapp.presentation.screen.home.HomeScreen
import com.example.fitnesapp.presentation.screen.home.HomeViewModel
import com.example.fitnesapp.presentation.screen.history.WorkoutHistoryDetailsScreen
import com.example.fitnesapp.presentation.screen.history.WorkoutHistoryDetailsViewModel
import com.example.fitnesapp.presentation.screen.history.WorkoutHistoryScreen
import com.example.fitnesapp.presentation.screen.history.WorkoutHistoryViewModel
import com.example.fitnesapp.presentation.screen.notes.NotesScreen
import com.example.fitnesapp.presentation.screen.notes.NotesViewModel
import com.example.fitnesapp.presentation.screen.progress.ProgressScreen
import com.example.fitnesapp.presentation.screen.progress.ProgressViewModel
import com.example.fitnesapp.presentation.screen.result.WorkoutResultScreen
import com.example.fitnesapp.presentation.screen.result.WorkoutResultViewModel
import com.example.fitnesapp.presentation.screen.settings.SettingsScreen
import com.example.fitnesapp.presentation.screen.settings.SettingsViewModel
import com.example.fitnesapp.presentation.screen.start.StartWorkoutScreen
import com.example.fitnesapp.presentation.screen.start.StartWorkoutViewModel
import com.example.fitnesapp.presentation.screen.workout.WorkoutPlanScreen
import com.example.fitnesapp.presentation.screen.workout.WorkoutPlanViewModel
import com.example.fitnesapp.presentation.theme.FitnesAppTheme
import com.example.fitnesapp.presentation.theme.ThemeViewModel

@Composable
fun FitnesApp(container: AppContainer) {
    val themeVm: ThemeViewModel = viewModel(factory = AppViewModelFactory(container))
    val theme by themeVm.theme.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    FitnesAppTheme(themeMode = theme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = Destinations.Home) {
        composable(Destinations.Home) {
            val vm: HomeViewModel = viewModel(factory = AppViewModelFactory(container))
            HomeScreen(
                viewModel = vm,
                onOpenExercises = { navController.navigate(Destinations.Exercises) },
                onOpenWorkouts = { navController.navigate(Destinations.Workouts) },
                onOpenHistory = { navController.navigate(Destinations.History) },
                onOpenProgress = { navController.navigate(Destinations.Progress) },
                onOpenNotes = { navController.navigate(Destinations.Notes) },
                onOpenSettings = { navController.navigate(Destinations.Settings) },
                onStartWorkout = { navController.navigate(Destinations.StartWorkout) }
            )
        }
        composable(Destinations.Exercises) {
            val vm: ExercisesViewModel = viewModel(factory = AppViewModelFactory(container))
            ExercisesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Destinations.Workouts) {
            val vm: WorkoutPlanViewModel = viewModel(factory = AppViewModelFactory(container))
            WorkoutPlanScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Destinations.Progress) {
            val vm: ProgressViewModel = viewModel(factory = AppViewModelFactory(container))
            ProgressScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Destinations.History) {
            val vm: WorkoutHistoryViewModel = viewModel(factory = AppViewModelFactory(container))
            WorkoutHistoryScreen(vm, onBack = { navController.popBackStack() }, onOpenDetails = { navController.navigate(Destinations.historyDetails(it)) })
        }
        composable(
            Destinations.HistoryDetails,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val vm: WorkoutHistoryDetailsViewModel = viewModel(factory = AppViewModelFactory(container, sessionId))
            WorkoutHistoryDetailsScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Destinations.Notes) {
            val vm: NotesViewModel = viewModel(factory = AppViewModelFactory(container))
            NotesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Destinations.Settings) {
            val vm: SettingsViewModel = viewModel(factory = AppViewModelFactory(container))
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Destinations.StartWorkout) {
            val vm: StartWorkoutViewModel = viewModel(factory = AppViewModelFactory(container))
            StartWorkoutScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenWorkout = { sessionId ->
                    navController.navigate(Destinations.activeWorkout(sessionId)) {
                        popUpTo(Destinations.StartWorkout) { inclusive = true }
                    }
                }
            )
        }
        composable(
            Destinations.ActiveWorkout,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val vm: ActiveWorkoutViewModel = viewModel(factory = AppViewModelFactory(container, sessionId))
            ActiveWorkoutScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onCompleted = { completedSessionId ->
                    navController.navigate(Destinations.result(completedSessionId)) {
                        popUpTo(Destinations.Home)
                    }
                }
            )
        }
        composable(
            Destinations.Result,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val vm: WorkoutResultViewModel = viewModel(factory = AppViewModelFactory(container, sessionId))
            WorkoutResultScreen(
                viewModel = vm,
                onFinish = {
                    navController.navigate(Destinations.Home) {
                        popUpTo(0)
                    }
                }
            )
        }
            }
        }
    }
}
