package com.example.fitnesapp.presentation.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.LoadUnit
import com.example.fitnesapp.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExercisesUiState(
    val items: List<Exercise> = emptyList(),
    val message: String? = null
)

class ExercisesViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ExercisesUiState())
    val state: StateFlow<ExercisesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeExercises().collect { exercises ->
                _state.update { it.copy(items = exercises) }
            }
        }
    }

    fun saveExercise(
        editingId: Long?,
        name: String,
        muscleGroup: String,
        type: ExerciseType,
        description: String,
        loadUnit: LoadUnit
    ) {
        if (name.isBlank()) {
            _state.update { it.copy(message = "Название упражнения обязательно") }
            return
        }
        viewModelScope.launch {
            repository.saveExercise(
                Exercise(
                    id = editingId ?: 0L,
                    name = name.trim(),
                    muscleGroup = muscleGroup.trim(),
                    type = type,
                    description = description.trim(),
                    loadUnit = loadUnit
                )
            )
            _state.update { it.copy(message = "Упражнение сохранено") }
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            val deleted = repository.deleteExercise(id)
            _state.update {
                it.copy(message = if (deleted) "Упражнение удалено" else "Упражнение используется в плане и было архивировано")
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
