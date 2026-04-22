package com.example.fitnesapp.presentation.screen.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.LoadUnit
import com.example.fitnesapp.domain.model.ProgramTemplate
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutPlanExercise
import com.example.fitnesapp.domain.model.label
import com.example.fitnesapp.domain.repository.ExerciseRepository
import com.example.fitnesapp.domain.repository.WorkoutPlanRepository
import com.example.fitnesapp.domain.usecase.GetProgramTemplatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutPlanUiState(
    val days: List<WorkoutPlanDay> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val templates: List<ProgramTemplate> = emptyList(),
    val message: String? = null
)

class WorkoutPlanViewModel(
    private val repository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val getProgramTemplatesUseCase: GetProgramTemplatesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutPlanUiState())
    val state: StateFlow<WorkoutPlanUiState> = _state.asStateFlow()

    init {
        _state.update { it.copy(templates = getProgramTemplatesUseCase()) }
        viewModelScope.launch {
            combine(repository.observePlan(), exerciseRepository.observeExercises()) { days, exercises ->
                WorkoutPlanUiState(days = days, exercises = exercises.filterNot { it.isArchived }, templates = _state.value.templates)
            }.collect { _state.value = it }
        }
    }

    fun saveDay(dayOfWeek: Int, isRest: Boolean, items: List<WorkoutPlanExercise>) {
        viewModelScope.launch {
            repository.saveDay(WorkoutPlanDay(dayOfWeek, isRest, items.sortedBy { it.orderInDay }))
            _state.update { it.copy(message = "День плана сохранен") }
        }
    }

    fun buildExercise(exercise: Exercise, dayOfWeek: Int, order: Int): WorkoutPlanExercise = WorkoutPlanExercise(
        dayOfWeek = dayOfWeek,
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        muscleGroupTag = exercise.muscleGroupTag,
        exerciseType = exercise.type,
        loadUnit = exercise.loadUnit,
        orderInDay = order,
        targetSets = if (exercise.type == ExerciseType.CARDIO) 1 else 4,
        targetReps = if (exercise.loadUnit == LoadUnit.KG) 8 else 20,
        targetWeight = if (exercise.loadUnit == LoadUnit.KG) 60.0 else null,
        restSeconds = 120,
        note = ""
    )

    fun applyTemplate(template: ProgramTemplate) {
        viewModelScope.launch {
            val resolvedDays = template.days.map { day ->
                day.copy(
                    exercises = day.exercises.mapIndexed { index, item ->
                        val resolved = exerciseRepository.ensureExercise(
                            Exercise(
                                name = item.exerciseName,
                                muscleGroup = item.muscleGroupTag.label(),
                                muscleGroupTag = item.muscleGroupTag,
                                type = item.exerciseType,
                                loadUnit = item.loadUnit,
                                description = "Системное упражнение шаблона",
                                isSystem = true
                            )
                        )
                        item.copy(
                            dayOfWeek = day.dayOfWeek,
                            orderInDay = index + 1,
                            exerciseId = resolved.id,
                            exerciseName = resolved.name,
                            muscleGroupTag = resolved.muscleGroupTag,
                            exerciseType = resolved.type,
                            loadUnit = resolved.loadUnit
                        )
                    }
                )
            }
            repository.applyTemplate(resolvedDays)
            _state.update { it.copy(message = "Шаблон применен") }
        }
    }

    fun copyDay(sourceDayOfWeek: Int, targetDayOfWeek: Int, overwrite: Boolean = true) {
        viewModelScope.launch {
            repository.copyWorkoutDay(sourceDayOfWeek, targetDayOfWeek, overwrite)
            _state.update { it.copy(message = "День скопирован") }
        }
    }

    fun duplicateExercise(dayOfWeek: Int, exerciseOrder: Int) {
        viewModelScope.launch {
            repository.duplicateWorkoutExercise(dayOfWeek, exerciseOrder)
            _state.update { it.copy(message = "Упражнение продублировано") }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
