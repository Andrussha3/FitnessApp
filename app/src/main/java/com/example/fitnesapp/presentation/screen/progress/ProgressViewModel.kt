package com.example.fitnesapp.presentation.screen.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.GoalFocusedMetrics
import com.example.fitnesapp.domain.model.MuscleGroupStats
import com.example.fitnesapp.domain.model.ProgressReport
import com.example.fitnesapp.domain.repository.ExerciseRepository
import com.example.fitnesapp.domain.repository.ProgressRepository
import com.example.fitnesapp.domain.usecase.GetGoalFocusedMetricsUseCase
import com.example.fitnesapp.domain.usecase.GetMuscleGroupStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseId: Long? = null,
    val selectedPeriodDays: Int? = 30,
    val report: ProgressReport = ProgressReport(),
    val muscleStats: List<MuscleGroupStats> = emptyList(),
    val goalMetrics: List<GoalFocusedMetrics> = emptyList()
)

class ProgressViewModel(
    private val progressRepository: ProgressRepository,
    private val exerciseRepository: ExerciseRepository,
    private val getMuscleGroupStatsUseCase: GetMuscleGroupStatsUseCase,
    private val getGoalFocusedMetricsUseCase: GetGoalFocusedMetricsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.observeExercises().collect { exercises ->
                _state.update { it.copy(exercises = exercises) }
            }
        }
        viewModelScope.launch {
            _state.flatMapLatest { progressRepository.observeProgress(it.selectedExerciseId, it.selectedPeriodDays) }
                .collect { report -> _state.update { it.copy(report = report) } }
        }
        viewModelScope.launch {
            _state.flatMapLatest { combine(getMuscleGroupStatsUseCase(it.selectedPeriodDays), getGoalFocusedMetricsUseCase(it.selectedPeriodDays)) { muscle, goal -> muscle to goal } }
                .collect { (muscle, goal) -> _state.update { state -> state.copy(muscleStats = muscle, goalMetrics = goal) } }
        }
    }

    fun selectExercise(id: Long?) {
        _state.update { it.copy(selectedExerciseId = id) }
    }

    fun selectPeriod(days: Int?) {
        _state.update { it.copy(selectedPeriodDays = days) }
    }
}
