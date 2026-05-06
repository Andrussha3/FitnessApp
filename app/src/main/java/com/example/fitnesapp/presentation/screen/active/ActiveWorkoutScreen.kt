package com.example.fitnesapp.presentation.screen.active

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.domain.model.WorkoutSet
import com.example.fitnesapp.domain.model.WorkoutStage
import com.example.fitnesapp.presentation.component.AppTextField
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.SectionCard
import com.example.fitnesapp.presentation.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onBack: () -> Unit,
    onCompleted: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.completedSessionId) {
        state.completedSessionId?.let {
            onCompleted(it)
            viewModel.consumeCompletion()
        }
    }

    val session = state.session
    val exercise = state.currentExercise
    val set = state.currentSet
    var replacing by remember { mutableStateOf(false) }
    var choosingExercise by remember { mutableStateOf(false) }
    var confirmFinishEarly by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.dayLabel ?: "Тренировка") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (session == null || exercise == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("Нет активной тренировки", "Незавершенная тренировка не найдена")
            }
            return@Scaffold
        }

        when (state.stage) {
            WorkoutStage.INPUT_SET -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionCard(
                        title = exercise.exerciseName,
                        subtitle = "Упражнение ${state.currentExerciseNumber} из ${state.totalExercises}",
                        modifier = Modifier.pointerInput(exercise.id) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (kotlin.math.abs(dragAmount) > 24f) {
                                    choosingExercise = true
                                }
                            }
                        }
                    ) {
                        if (exercise.isReplacement) {
                            Text("Заменено во время тренировки. План: ${exercise.plannedExerciseName}")
                        }
                        state.latestRecommendationText?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Text("Текущий подход: ${set?.orderInExercise ?: 0} из ${exercise.sets.size}")
                        Text("Свайп по карточке открывает выбор упражнения", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (exercise.note.isNotBlank()) {
                            Text(exercise.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { replacing = true }) { Text("Заменить упражнение") }
                        TextButton(onClick = { choosingExercise = true }) { Text("Выбрать другое упражнение") }
                        TextButton(onClick = { confirmFinishEarly = true }) { Text("Завершить тренировку") }
                    }
                    SectionCard(title = "Подходы") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            exercise.sets.forEach { item ->
                                SetStatusRow(item, isActive = item.id == set?.id)
                            }
                        }
                    }
                    if (set != null) {
                        SectionCard(title = "Ввод текущего подхода") {
                            AppTextField(state.inputWeight, viewModel::onInputWeightChanged, "Фактический вес")
                            AppTextField(state.inputReps, viewModel::onInputRepsChanged, "Фактические повторения")
                            Button(onClick = { viewModel.completeCurrentSet(state.inputReps, state.inputWeight) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Подход выполнен")
                            }
                            OutlinedButton(onClick = { viewModel.skipCurrentSet() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Пропустить подход")
                            }
                            TextButton(onClick = { viewModel.skipCurrentExercise() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Пропустить упражнение")
                            }
                        }
                    }
                }
            }

            WorkoutStage.REST_TIMER -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                    SectionCard(
                        title = exercise.exerciseName,
                        subtitle = "Завершен подход ${(set?.orderInExercise ?: 1) - 1} из ${exercise.sets.size}",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "${state.restSecondsLeft} сек",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { viewModel.skipRest() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Пропустить отдых")
                            }
                            TextButton(onClick = { confirmFinishEarly = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Завершить тренировку")
                            }
                        }
                    }
                }
            }

            WorkoutStage.EXERCISE_FINISHED -> Unit

            WorkoutStage.WORKOUT_FINISHED -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                    SectionCard(title = "Тренировка завершается", subtitle = "Подводим итоги") {
                        Text("Сохраняем результат и открываем экран завершения")
                    }
                }
            }
        }
    }
    if (replacing) {
        AlertDialog(
            onDismissRequest = { replacing = false },
            title = { Text("Заменить упражнение") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.replacements.sortedBy { if (it.muscleGroupTag == exercise?.muscleGroupTag) 0 else 1 }.forEach { item ->
                        Text(
                            text = "${item.name} • ${item.muscleGroup}",
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.replaceExercise(item)
                                replacing = false
                            }.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { replacing = false }) { Text("Отмена") } }
        )
    }
    if (choosingExercise) {
        AlertDialog(
            onDismissRequest = { choosingExercise = false },
            title = { Text("Упражнения тренировки") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.exerciseStatuses.forEachIndexed { index, item ->
                        val status = when {
                            item.id == exercise?.id -> "Текущее"
                            item.sets.any { !it.completed && !it.skipped } -> "Не завершено"
                            item.sets.any { it.completed } -> "Завершено"
                            else -> "Пропущено"
                        }
                        val statusColors = exerciseStatusColors(status)
                        Text(
                            text = "${index + 1}. ${item.exerciseName} • $status",
                            color = statusColors.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(statusColors.background, RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.selectExercise(index)
                                    choosingExercise = false
                                }
                                .padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { choosingExercise = false }) { Text("Закрыть") } }
        )
    }
    if (confirmFinishEarly) {
        AlertDialog(
            onDismissRequest = { confirmFinishEarly = false },
            title = { Text("Завершить тренировку досрочно?") },
            text = { Text("Тренировка будет сохранена в текущем состоянии. Невыполненные упражнения останутся пропущенными или незавершенными в истории.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.finishWorkoutEarly()
                    confirmFinishEarly = false
                }) { Text("Завершить") }
            },
            dismissButton = { TextButton(onClick = { confirmFinishEarly = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun SetStatusRow(set: WorkoutSet, isActive: Boolean) {
    val status = when {
        set.completed -> "Выполнен"
        set.skipped -> "Пропущен"
        isActive -> "Текущий"
        else -> "Ожидает"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else SurfaceDark, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text("Подход ${set.orderInExercise} • $status")
    }
}

private data class ExerciseStatusColors(
    val background: Color,
    val text: Color
)

@Composable
private fun exerciseStatusColors(status: String): ExerciseStatusColors {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        "Текущее" -> ExerciseStatusColors(
            background = scheme.primary.copy(alpha = 0.18f),
            text = scheme.onSurface
        )
        "Не завершено" -> ExerciseStatusColors(
            background = scheme.secondary.copy(alpha = 0.14f),
            text = scheme.onSurface
        )
        "Завершено" -> ExerciseStatusColors(
            background = Color(0xFF27412E).copy(alpha = 0.85f),
            text = Color(0xFFE7F6EA)
        )
        else -> ExerciseStatusColors(
            background = scheme.surfaceVariant.copy(alpha = 0.9f),
            text = scheme.onSurfaceVariant
        )
    }
}
