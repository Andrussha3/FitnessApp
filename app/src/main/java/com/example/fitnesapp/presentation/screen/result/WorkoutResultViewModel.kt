package com.example.fitnesapp.presentation.screen.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.PersonalRecord
import com.example.fitnesapp.domain.model.ProgressionRecommendation
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.model.strengthVolume
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.usecase.GetWorkoutRecordsUseCase
import com.example.fitnesapp.domain.usecase.GetWorkoutRecommendationsUseCase
import com.example.fitnesapp.domain.usecase.SaveCompletedWorkoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutResultUiState(
    val session: WorkoutSession? = null,
    val message: String = "",
    val recommendations: List<ProgressionRecommendation> = emptyList(),
    val records: List<PersonalRecord> = emptyList()
)

class WorkoutResultViewModel(
    private val sessionId: Long,
    private val sessionRepository: SessionRepository,
    private val saveCompletedWorkoutUseCase: SaveCompletedWorkoutUseCase,
    private val getWorkoutRecommendationsUseCase: GetWorkoutRecommendationsUseCase,
    private val getWorkoutRecordsUseCase: GetWorkoutRecordsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutResultUiState())
    val state: StateFlow<WorkoutResultUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            saveCompletedWorkoutUseCase(sessionId)
            val session = sessionRepository.getSession(sessionId)
            launch {
                getWorkoutRecommendationsUseCase(sessionId).collect { items ->
                    _state.update { it.copy(recommendations = items) }
                }
            }
            launch {
                getWorkoutRecordsUseCase(sessionId).collect { items ->
                    _state.update { it.copy(records = items) }
                }
            }
            sessionRepository.observeHistory().collect { sessions ->
                val current = session ?: return@collect
                val previous = sessions.firstOrNull { it.dayOfWeek == current.dayOfWeek && it.id != current.id }
                val currentVolume = current.strengthVolume()
                val previousVolume = previous?.strengthVolume()
                val message = when {
                    previousVolume == null || previousVolume == 0.0 -> "Это ваша первая зафиксированная тренировка этого типа"
                    currentVolume > previousVolume -> "Сегодня вы молодец, вы превзошли свой прошлый результат на ${"%.1f".format(((currentVolume - previousVolume) / previousVolume) * 100)}%"
                    currentVolume < previousVolume -> "Сегодня вы, возможно, устали - в прошлый раз вы были сильнее на ${"%.1f".format(((previousVolume - currentVolume) / previousVolume) * 100)}%"
                    else -> "Сегодня результат на уровне прошлой тренировки"
                }
                _state.update { it.copy(session = current, message = message) }
            }
        }
    }
}
