package com.example.fitnesapp.presentation.screen.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutPlanExercise
import com.example.fitnesapp.domain.model.dayLabel
import com.example.fitnesapp.presentation.component.AppTextField
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanScreen(viewModel: WorkoutPlanViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingDay by remember { mutableStateOf<WorkoutPlanDay?>(null) }
    var applyingTemplate by remember { mutableStateOf(false) }
    var copyingDay by remember { mutableStateOf<WorkoutPlanDay?>(null) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои тренировки") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TextButton(onClick = { applyingTemplate = true }) { Text("Готовые программы") }
            }
            items(state.days, key = { it.dayOfWeek }) { day ->
                SectionCard(title = day.title, subtitle = if (day.isRestDay) "День отдыха" else "${day.exercises.size} упражнений") {
                    if (day.isRestDay || day.exercises.isEmpty()) {
                        EmptyState("Отдых", "На этот день тренировочная нагрузка не назначена")
                    } else {
                        day.exercises.sortedBy { it.orderInDay }.forEach {
                            Text("${it.orderInDay}. ${it.exerciseName} • ${it.targetSets}x${it.targetReps ?: "-"} • отдых ${it.restSeconds}с")
                        }
                    }
                    TextButton(onClick = { editingDay = day }) { Text("Редактировать день") }
                    TextButton(onClick = { copyingDay = day }) { Text("Копировать день") }
                }
            }
        }
    }
    editingDay?.let { day ->
        DayEditorDialog(viewModel, day, onDismiss = { editingDay = null })
    }
    if (applyingTemplate) {
        AlertDialog(
            onDismissRequest = { applyingTemplate = false },
            title = { Text("Готовые программы") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.templates.forEach { template ->
                        TextButton(onClick = { viewModel.applyTemplate(template); applyingTemplate = false }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                            Text(if (template.isRecommended) "${template.name} • Рекомендуемый" else template.name)
                        }
                        Text(template.description)
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { applyingTemplate = false }) { Text("Закрыть") } }
        )
    }
    copyingDay?.let { source ->
        AlertDialog(
            onDismissRequest = { copyingDay = null },
            title = { Text("Копировать день") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..7).filter { it != source.dayOfWeek }.forEach { day ->
                        TextButton(onClick = { viewModel.copyDay(source.dayOfWeek, day, true); copyingDay = null }) { Text(day.dayLabel()) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { copyingDay = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun DayEditorDialog(viewModel: WorkoutPlanViewModel, day: WorkoutPlanDay, onDismiss: () -> Unit) {
    var isRest by remember(day) { mutableStateOf(day.isRestDay) }
    val items = remember(day) { mutableStateListOf<WorkoutPlanExercise>().apply { addAll(day.exercises.sortedBy { it.orderInDay }) } }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var addExercise by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(day.dayOfWeek.dayLabel()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("День отдыха")
                    Switch(checked = isRest, onCheckedChange = { isRest = it })
                }
                if (!isRest) {
                    if (items.isEmpty()) {
                        EmptyState("Пустой день", "Добавьте упражнения из вашего списка")
                    }
                    items.forEachIndexed { index, item ->
                        SectionCard(title = item.exerciseName, subtitle = "${item.targetSets} подходов") {
                            var sets by remember(item.id, index) { mutableStateOf(item.targetSets.toString()) }
                            var reps by remember(item.id, index) { mutableStateOf(item.targetReps?.toString().orEmpty()) }
                            var weight by remember(item.id, index) { mutableStateOf(item.targetWeight?.toString().orEmpty()) }
                            var rest by remember(item.id, index) { mutableStateOf(item.restSeconds.toString()) }
                            var note by remember(item.id, index) { mutableStateOf(item.note) }
                            AppTextField(sets, { sets = it.filter(Char::isDigit); items[index] = items[index].copy(targetSets = sets.toIntOrNull() ?: 1) }, "Подходы")
                            AppTextField(reps, { reps = it.filter(Char::isDigit); items[index] = items[index].copy(targetReps = reps.toIntOrNull()) }, "Повторения")
                            AppTextField(weight, { weight = it; items[index] = items[index].copy(targetWeight = weight.replace(',', '.').toDoubleOrNull()) }, "Вес")
                            AppTextField(rest, { rest = it.filter(Char::isDigit); items[index] = items[index].copy(restSeconds = rest.toIntOrNull() ?: 120) }, "Отдых, сек")
                            AppTextField(note, { note = it; items[index] = items[index].copy(note = it) }, "Заметка")
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    if (index > 0) {
                                        val current = items[index]
                                        items[index] = items[index - 1].copy(orderInDay = index + 1)
                                        items[index - 1] = current.copy(orderInDay = index)
                                    }
                                }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                                IconButton(onClick = {
                                    if (index < items.lastIndex) {
                                        val current = items[index]
                                        items[index] = items[index + 1].copy(orderInDay = index + 1)
                                        items[index + 1] = current.copy(orderInDay = index + 2)
                                    }
                                }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                                IconButton(onClick = { viewModel.duplicateExercise(day.dayOfWeek, item.orderInDay) }) { Icon(Icons.Default.ContentCopy, null) }
                                TextButton(onClick = { items.removeAt(index) }) { Text("Удалить") }
                            }
                        }
                    }
                    TextButton(onClick = { addExercise = true }) { Text("Добавить упражнение") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.saveDay(
                    day.dayOfWeek,
                    isRest,
                    items.mapIndexed { index, item -> item.copy(orderInDay = index + 1) }
                )
                onDismiss()
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
    if (addExercise) {
        AlertDialog(
            onDismissRequest = { addExercise = false },
            title = { Text("Добавить упражнение") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.exercises.forEach { exercise ->
                        TextButton(onClick = {
                            items += viewModel.buildExercise(exercise, day.dayOfWeek, items.size + 1)
                            addExercise = false
                        }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                            Text(exercise.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { addExercise = false }) { Text("Закрыть") } }
        )
    }
}
