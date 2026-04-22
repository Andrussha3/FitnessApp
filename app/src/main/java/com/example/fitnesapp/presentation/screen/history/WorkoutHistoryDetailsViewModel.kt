package com.example.fitnesapp.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.WorkoutHistoryDetails
import com.example.fitnesapp.domain.usecase.GetWorkoutHistoryDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutHistoryDetailsUiState(
    val details: WorkoutHistoryDetails? = null
)

class WorkoutHistoryDetailsViewModel(
    private val sessionId: Long,
    private val getWorkoutHistoryDetailsUseCase: GetWorkoutHistoryDetailsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutHistoryDetailsUiState())
    val state: StateFlow<WorkoutHistoryDetailsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = WorkoutHistoryDetailsUiState(getWorkoutHistoryDetailsUseCase(sessionId))
        }
    }
}
