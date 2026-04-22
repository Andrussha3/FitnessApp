package com.example.fitnesapp.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.TodayWorkoutDecision
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.repository.ProfileRepository
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.repository.WorkoutPlanRepository
import com.example.fitnesapp.domain.usecase.GetTodayWorkoutUseCase
import com.example.fitnesapp.domain.usecase.SeedDemoDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val profileName: String = "Спортсмен",
    val todayMessage: String = "",
    val activeSession: WorkoutSession? = null,
    val workoutsDone: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val sessionRepository: SessionRepository,
    private val seedDemoDataUseCase: SeedDemoDataUseCase,
    private val getTodayWorkoutUseCase: GetTodayWorkoutUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                sessionRepository.observeActiveSession(),
                sessionRepository.observeHistory(),
                workoutPlanRepository.observePlan()
            ) { profile, active, history, _ ->
                val decision: TodayWorkoutDecision = getTodayWorkoutUseCase()
                HomeUiState(
                    profileName = profile?.name?.ifBlank { "Спортсмен" } ?: "Спортсмен",
                    todayMessage = decision.message,
                    activeSession = active,
                    workoutsDone = history.size,
                    isLoading = false
                )
            }.collectLatest { _state.value = it }
        }
    }
}
