package com.example.fitnesapp.presentation.screen.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.data.notification.RestTimerScheduler
import com.example.fitnesapp.domain.model.WorkoutComparison
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.WorkoutSession
import com.example.fitnesapp.domain.model.WorkoutSessionExercise
import com.example.fitnesapp.domain.model.WorkoutSet
import com.example.fitnesapp.domain.model.WorkoutStage
import com.example.fitnesapp.domain.repository.ExerciseRepository
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.domain.repository.WorkoutInsightsRepository
import com.example.fitnesapp.domain.usecase.CompleteWorkoutUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val session: WorkoutSession? = null,
    val stage: WorkoutStage = WorkoutStage.INPUT_SET,
    val currentExercise: WorkoutSessionExercise? = null,
    val currentSet: WorkoutSet? = null,
    val currentExerciseNumber: Int = 0,
    val totalExercises: Int = 0,
    val exerciseStatuses: List<WorkoutSessionExercise> = emptyList(),
    val restSecondsLeft: Int = 0,
    val replacements: List<Exercise> = emptyList(),
    val latestRecommendationText: String? = null,
    val inputWeight: String = "",
    val inputReps: String = "",
    val completedSessionId: Long? = null,
    val comparison: WorkoutComparison? = null
)

class ActiveWorkoutViewModel(
    private val sessionId: Long,
    private val sessionRepository: SessionRepository,
    private val completeWorkoutUseCase: CompleteWorkoutUseCase,
    private val exerciseRepository: ExerciseRepository,
    private val workoutInsightsRepository: WorkoutInsightsRepository,
    private val scheduler: RestTimerScheduler
) : ViewModel() {
    private val _state = MutableStateFlow(ActiveWorkoutUiState())
    val state: StateFlow<ActiveWorkoutUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var pendingNextExerciseIndex: Int? = null

    init {
        viewModelScope.launch {
            exerciseRepository.observeExercises().collect { items -> _state.update { it.copy(replacements = items.filterNot { e -> e.isArchived }) } }
        }
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collectLatest { session ->
                if (session == null) {
                    _state.update { it.copy(session = null) }
                    return@collectLatest
                }
                val currentExercise = session.exercises.getOrNull(session.currentExerciseIndex)
                val currentSet = currentExercise?.sets?.firstOrNull { !it.completed && !it.skipped }
                if (pendingNextExerciseIndex != null && session.currentExerciseIndex == pendingNextExerciseIndex) {
                    pendingNextExerciseIndex = null
                }
                _state.update {
                    it.copy(
                        session = session,
                        stage = session.currentStage,
                        currentExercise = currentExercise,
                        currentSet = currentSet,
                        currentExerciseNumber = (session.currentExerciseIndex + 1).coerceAtMost(session.exercises.size),
                        totalExercises = session.exercises.size,
                        exerciseStatuses = session.exercises,
                        latestRecommendationText = currentExercise?.exerciseId?.let { id -> null },
                        inputWeight = suggestedWeightForInput(currentExercise, currentSet),
                        inputReps = suggestedRepsForInput(currentExercise, currentSet)
                    )
                }
                currentExercise?.exerciseId?.let { exerciseId ->
                    val latest = workoutInsightsRepository.getLatestRecommendationForExercise(exerciseId)
                    _state.update { it.copy(latestRecommendationText = latest?.message) }
                }
                val canAutoAdvanceFinishedExercise =
                    session.currentStage == WorkoutStage.INPUT_SET &&
                        currentExercise != null &&
                        currentExercise.sets.isNotEmpty() &&
                        currentSet == null &&
                        pendingNextExerciseIndex == null

                if (canAutoAdvanceFinishedExercise) {
                    advanceOrFinish(session)
                    return@collectLatest
                }
                when (session.currentStage) {
                    WorkoutStage.INPUT_SET -> stopTimerLocally()
                    WorkoutStage.REST_TIMER -> startTimer(session.restTimerEndAt)
                    WorkoutStage.EXERCISE_FINISHED -> advanceOrFinish(session)
                    WorkoutStage.WORKOUT_FINISHED -> finishWorkout(session)
                }
            }
        }
    }

    fun completeCurrentSet(reps: String, weight: String) {
        val session = _state.value.session ?: return
        val exercise = _state.value.currentExercise ?: return
        val set = _state.value.currentSet ?: return
        viewModelScope.launch {
            sessionRepository.updateSet(
                setId = set.id,
                actualReps = reps.toIntOrNull(),
                actualWeight = weight.replace(',', '.').toDoubleOrNull(),
                completed = true,
                skipped = false
            )
            _state.update { it.copy(inputReps = reps, inputWeight = weight) }
            moveAfterSetAction(session, exercise, set.orderInExercise)
        }
    }

    fun skipCurrentSet() {
        val session = _state.value.session ?: return
        val exercise = _state.value.currentExercise ?: return
        val set = _state.value.currentSet ?: return
        viewModelScope.launch {
            sessionRepository.updateSet(
                setId = set.id,
                actualReps = null,
                actualWeight = null,
                completed = false,
                skipped = true
            )
            moveAfterSetAction(session, exercise, set.orderInExercise)
        }
    }

    fun onInputWeightChanged(value: String) {
        _state.update { it.copy(inputWeight = value) }
    }

    fun onInputRepsChanged(value: String) {
        _state.update { it.copy(inputReps = value.filter(Char::isDigit)) }
    }

    fun skipCurrentExercise() {
        val session = _state.value.session ?: return
        val exercise = _state.value.currentExercise ?: return
        viewModelScope.launch {
            exercise.sets.filter { !it.completed && !it.skipped }.forEach { set ->
                sessionRepository.updateSet(
                    setId = set.id,
                    actualReps = null,
                    actualWeight = null,
                    completed = false,
                    skipped = true
                )
            }
            sessionRepository.markExerciseSkipped(exercise.id, true)
            advanceOrFinish(session)
        }
    }

    fun skipRest() {
        viewModelScope.launch {
            scheduler.cancel()
            sessionRepository.setRestTimer(sessionId, null)
            sessionRepository.setStage(sessionId, WorkoutStage.INPUT_SET)
        }
    }

    fun replaceExercise(exercise: Exercise) {
        val current = _state.value.currentExercise ?: return
        viewModelScope.launch {
            sessionRepository.replaceCurrentExercise(sessionId, current.id, exercise)
        }
    }

    fun selectExercise(exerciseIndex: Int) {
        val session = _state.value.session ?: return
        viewModelScope.launch {
            scheduler.cancel()
            pendingNextExerciseIndex = null
            sessionRepository.setRestTimer(session.id, null)
            sessionRepository.setCurrentExercise(session.id, exerciseIndex.coerceIn(0, session.exercises.lastIndex))
            sessionRepository.setStage(session.id, WorkoutStage.INPUT_SET)
        }
    }

    fun finishWorkoutEarly() {
        val session = _state.value.session ?: return
        viewModelScope.launch {
            sessionRepository.setStage(session.id, WorkoutStage.WORKOUT_FINISHED)
            finishWorkout(session)
        }
    }

    private suspend fun moveAfterSetAction(
        session: WorkoutSession,
        exercise: WorkoutSessionExercise,
        finishedSetOrder: Int
    ) {
        val isLastSet = finishedSetOrder >= exercise.sets.size
        if (isLastSet) {
            advanceOrFinish(session)
            return
        }
        val restSeconds = exercise.restSeconds.takeIf { it > 0 } ?: 120
        val endAt = System.currentTimeMillis() + restSeconds * 1000L
        sessionRepository.setRestTimer(session.id, endAt)
        sessionRepository.setStage(session.id, WorkoutStage.REST_TIMER)
        scheduler.schedule(endAt)
    }

    private fun startTimer(endAt: Long?) {
        timerJob?.cancel()
        val safeEndAt = endAt ?: run {
            viewModelScope.launch { sessionRepository.setStage(sessionId, WorkoutStage.INPUT_SET) }
            return
        }
        timerJob = viewModelScope.launch {
            while (true) {
                val left = ((safeEndAt - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
                _state.update { it.copy(restSecondsLeft = left) }
                if (left <= 0) {
                    scheduler.cancel()
                    sessionRepository.setRestTimer(sessionId, null)
                    sessionRepository.setStage(sessionId, WorkoutStage.INPUT_SET)
                    break
                }
                delay(1000)
            }
        }
    }

    private fun advanceOrFinish(session: WorkoutSession) {
        viewModelScope.launch {
            val nextIndex = findNextUnfinishedExerciseIndex(session, session.currentExerciseIndex)
            if (nextIndex == null) {
                sessionRepository.setStage(session.id, WorkoutStage.WORKOUT_FINISHED)
                finishWorkout(session)
            } else {
                pendingNextExerciseIndex = nextIndex
                sessionRepository.advanceToNextExercise(session.id, nextIndex)
            }
        }
    }

    private fun findNextUnfinishedExerciseIndex(session: WorkoutSession, currentIndex: Int): Int? {
        if (session.exercises.isEmpty()) return null
        val size = session.exercises.size
        for (offset in 1..size) {
            val index = (currentIndex + offset) % size
            val exercise = session.exercises[index]
            if (exercise.sets.any { !it.completed && !it.skipped }) {
                return index
            }
        }
        return null
    }

    private fun finishWorkout(session: WorkoutSession) {
        viewModelScope.launch {
            scheduler.cancel()
            stopTimerLocally()
            if (_state.value.completedSessionId != null) return@launch
            val comparison = completeWorkoutUseCase(session.id)
            _state.update {
                it.copy(
                    comparison = comparison,
                    completedSessionId = session.id,
                    stage = WorkoutStage.WORKOUT_FINISHED
                )
            }
        }
    }

    private fun stopTimerLocally() {
        timerJob?.cancel()
        _state.update { it.copy(restSecondsLeft = 0) }
    }

    fun consumeCompletion() {
        _state.update { it.copy(completedSessionId = null) }
    }

    private fun suggestedWeightForInput(exercise: WorkoutSessionExercise?, currentSet: WorkoutSet?): String {
        if (exercise == null || currentSet == null) return ""
        if (currentSet.actualWeight != null) return formatNumber(currentSet.actualWeight)
        val previous = exercise.sets
            .filter { it.orderInExercise < currentSet.orderInExercise }
            .mapNotNull { it.actualWeight ?: it.targetWeight }
            .lastOrNull()
        return previous?.let(::formatNumber) ?: currentSet.targetWeight?.let(::formatNumber).orEmpty()
    }

    private fun suggestedRepsForInput(exercise: WorkoutSessionExercise?, currentSet: WorkoutSet?): String {
        if (exercise == null || currentSet == null) return ""
        if (currentSet.actualReps != null) return currentSet.actualReps.toString()
        val previous = exercise.sets
            .filter { it.orderInExercise < currentSet.orderInExercise }
            .mapNotNull { it.actualReps ?: it.targetReps }
            .lastOrNull()
        return previous?.toString() ?: currentSet.targetReps?.toString().orEmpty()
    }

    private fun formatNumber(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
}
