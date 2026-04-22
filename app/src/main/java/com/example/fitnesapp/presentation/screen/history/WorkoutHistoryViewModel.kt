package com.example.fitnesapp.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.WorkoutHistoryItem
import com.example.fitnesapp.domain.usecase.GetWorkoutHistoryListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutHistoryUiState(
    val items: List<WorkoutHistoryItem> = emptyList()
)

class WorkoutHistoryViewModel(
    getWorkoutHistoryListUseCase: GetWorkoutHistoryListUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutHistoryUiState())
    val state: StateFlow<WorkoutHistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getWorkoutHistoryListUseCase().collect { _state.value = WorkoutHistoryUiState(it) }
        }
    }
}
