package com.example.fitnesapp.presentation.screen.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.TodayWorkoutDecision
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.usecase.GetTodayWorkoutUseCase
import com.example.fitnesapp.domain.usecase.StartWorkoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StartWorkoutUiState(
    val decision: TodayWorkoutDecision? = null,
    val navigateToSessionId: Long? = null,
    val isLoading: Boolean = true,
    val message: String? = null
)

class StartWorkoutViewModel(
    private val getTodayWorkoutUseCase: GetTodayWorkoutUseCase,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(StartWorkoutUiState())
    val state: StateFlow<StartWorkoutUiState> = _state.asStateFlow()

    init {
        refresh(autoStartPlannedWorkout = true)
    }

    fun refresh(autoStartPlannedWorkout: Boolean = false) {
        viewModelScope.launch {
            val decision = getTodayWorkoutUseCase()
            _state.value = StartWorkoutUiState(decision = decision, isLoading = false)
            if (autoStartPlannedWorkout && decision.plannedDay != null) {
                val sessionId = startWorkoutUseCase(decision.plannedDay)
                _state.update { it.copy(navigateToSessionId = sessionId) }
            }
        }
    }

    fun startPlannedWorkout() {
        val plannedDay = _state.value.decision?.plannedDay ?: return
        viewModelScope.launch {
            val sessionId = startWorkoutUseCase(plannedDay)
            _state.update { it.copy(navigateToSessionId = sessionId) }
        }
    }

    fun continueActive() {
        _state.update { it.copy(navigateToSessionId = it.decision?.activeSessionId) }
    }

    fun resetActiveWorkout() {
        val sessionId = _state.value.decision?.activeSessionId ?: return
        viewModelScope.launch {
            sessionRepository.cancelActiveSession(sessionId)
            val decision = getTodayWorkoutUseCase()
            _state.value = StartWorkoutUiState(
                decision = decision,
                isLoading = false,
                message = "Текущая тренировка сброшена"
            )
        }
    }

    fun consumeNavigation() {
        _state.update { it.copy(navigateToSessionId = null) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
