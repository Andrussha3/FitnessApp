package com.example.fitnesapp.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.AppThemeMode
import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.Gender
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.UserProfile
import com.example.fitnesapp.domain.repository.BodyMeasurementRepository
import com.example.fitnesapp.domain.repository.ProfileRepository
import com.example.fitnesapp.domain.repository.ThemeRepository
import com.example.fitnesapp.domain.usecase.ClearProgressDataUseCase
import com.example.fitnesapp.domain.usecase.ClearWorkoutHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profile: UserProfile = UserProfile(),
    val name: String = "",
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val goal: UserGoal = UserGoal.MAINTAIN,
    val goalNote: String = "",
    val measurements: List<BodyMeasurement> = emptyList(),
    val selectedTheme: AppThemeMode = AppThemeMode.STANDARD,
    val message: String? = null
)

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val bodyMeasurementRepository: BodyMeasurementRepository,
    private val themeRepository: ThemeRepository,
    private val clearProgressDataUseCase: ClearProgressDataUseCase,
    private val clearWorkoutHistoryUseCase: ClearWorkoutHistoryUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().collect { profile ->
                val safeProfile = profile ?: UserProfile()
                _state.update {
                    it.copy(
                        profile = safeProfile,
                        name = safeProfile.name,
                        age = safeProfile.age?.toString().orEmpty(),
                        height = safeProfile.heightCm?.toString().orEmpty(),
                        weight = safeProfile.weightKg?.toString().orEmpty(),
                        gender = safeProfile.gender,
                        goal = safeProfile.goal,
                        goalNote = safeProfile.goalNote
                    )
                }
            }
        }
        viewModelScope.launch {
            bodyMeasurementRepository.observeHistory().collect { items ->
                _state.update { it.copy(measurements = items) }
            }
        }
        viewModelScope.launch {
            themeRepository.observeTheme().collect { theme ->
                _state.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(name = value) }
    }

    fun onAgeChanged(value: String) {
        _state.update { it.copy(age = value.filter(Char::isDigit)) }
    }

    fun onHeightChanged(value: String) {
        _state.update { it.copy(height = value.filter(Char::isDigit)) }
    }

    fun onWeightChanged(value: String) {
        _state.update { it.copy(weight = value) }
    }

    fun onGenderChanged(value: Gender) {
        _state.update { it.copy(gender = value) }
    }

    fun onGoalChanged(value: UserGoal) {
        _state.update { it.copy(goal = value) }
    }

    fun onGoalNoteChanged(value: String) {
        _state.update { it.copy(goalNote = value) }
    }

    fun saveProfile() {
        val name = _state.value.name
        val gender = _state.value.gender
        val age = _state.value.age
        val height = _state.value.height
        val weight = _state.value.weight
        val goal = _state.value.goal
        val goalNote = _state.value.goalNote
        if (name.isBlank()) {
            _state.update { it.copy(message = "Имя обязательно") }
            return
        }
        viewModelScope.launch {
            profileRepository.saveProfile(
                UserProfile(
                    name = name.trim(),
                    gender = gender,
                    age = age.toIntOrNull(),
                    heightCm = height.toIntOrNull(),
                    weightKg = weight.replace(',', '.').toDoubleOrNull(),
                    goal = goal,
                    goalNote = goalNote,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _state.update { it.copy(message = "Профиль сохранен") }
        }
    }

    fun saveMeasurement(item: BodyMeasurement) {
        viewModelScope.launch {
            bodyMeasurementRepository.save(item)
            _state.update { it.copy(message = "Замер сохранен") }
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            bodyMeasurementRepository.delete(id)
            _state.update { it.copy(message = "Замер удален") }
        }
    }

    fun setTheme(theme: AppThemeMode) {
        viewModelScope.launch {
            themeRepository.setTheme(theme)
            _state.update { it.copy(message = "Тема обновлена") }
        }
    }

    fun clearProgressData() {
        viewModelScope.launch {
            clearProgressDataUseCase()
            _state.update { it.copy(message = "Данные прогресса очищены") }
        }
    }

    fun clearWorkoutHistory() {
        viewModelScope.launch {
            clearWorkoutHistoryUseCase()
            _state.update { it.copy(message = "История тренировок очищена") }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
