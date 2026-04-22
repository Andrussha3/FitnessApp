package com.example.fitnesapp.presentation.screen.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.domain.model.Exercise
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.LoadUnit
import com.example.fitnesapp.domain.model.label
import com.example.fitnesapp.presentation.component.AppTextField
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(viewModel: ExercisesViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Exercise?>(null) }
    var deleting by remember { mutableStateOf<Exercise?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои упражнения") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = Exercise(name = "") }) {
                Icon(Icons.Default.FitnessCenter, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.items.isEmpty()) {
                item { EmptyState("Пока нет упражнений", "Нажмите + и добавьте первое упражнение") }
            }
            items(state.items, key = { it.id }) { exercise ->
                SectionCard(title = exercise.name, subtitle = "${exercise.type.label()} • ${exercise.loadUnit.label()}") {
                    Text(exercise.muscleGroup.ifBlank { "Группа мышц не указана" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (exercise.description.isNotBlank()) {
                        Text(exercise.description)
                    }
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { editing = exercise }) {
                            Icon(Icons.Default.Edit, null)
                            Text("Редактировать")
                        }
                        TextButton(onClick = { deleting = exercise }) {
                            Icon(Icons.Default.Delete, null)
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
    editing?.let { item ->
        ExerciseEditorDialog(
            initial = item.takeIf { it.id != 0L },
            onDismiss = { editing = null },
            onSave = { name, muscle, type, desc, unit ->
                viewModel.saveExercise(item.takeIf { it.id != 0L }?.id, name, muscle, type, desc, unit)
                editing = null
            }
        )
    }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Удалить упражнение?") },
            text = { Text("Если упражнение уже используется в плане, оно будет мягко архивировано.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExercise(item.id)
                    deleting = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun ExerciseEditorDialog(
    initial: Exercise?,
    onDismiss: () -> Unit,
    onSave: (String, String, ExerciseType, String, LoadUnit) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var muscle by remember(initial) { mutableStateOf(initial?.muscleGroup.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.type ?: ExerciseType.STRENGTH) }
    var unit by remember(initial) { mutableStateOf(initial?.loadUnit ?: LoadUnit.KG) }
    val presets = listOf(
        Exercise(name = "Жим лежа", muscleGroup = "Грудь", type = ExerciseType.STRENGTH, description = "Базовое упражнение на грудь", loadUnit = LoadUnit.KG),
        Exercise(name = "Приседания", muscleGroup = "Ноги", type = ExerciseType.STRENGTH, description = "Базовое упражнение на ноги", loadUnit = LoadUnit.KG),
        Exercise(name = "Тяга верхнего блока", muscleGroup = "Спина", type = ExerciseType.STRENGTH, description = "Вертикальная тяга на широчайшие", loadUnit = LoadUnit.KG),
        Exercise(name = "Беговая дорожка", muscleGroup = "Кардио", type = ExerciseType.CARDIO, description = "Кардио по времени", loadUnit = LoadUnit.MINUTES)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новое упражнение" else "Редактирование") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = androidx.compose.ui.Modifier.verticalScroll(rememberScrollState())) {
                if (initial == null) {
                    Text("Быстрые варианты")
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.take(2).forEach { preset ->
                            FilterChip(
                                selected = name == preset.name,
                                onClick = {
                                    name = preset.name
                                    muscle = preset.muscleGroup
                                    description = preset.description
                                    type = preset.type
                                    unit = preset.loadUnit
                                },
                                label = { Text(preset.name) }
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.drop(2).forEach { preset ->
                            FilterChip(
                                selected = name == preset.name,
                                onClick = {
                                    name = preset.name
                                    muscle = preset.muscleGroup
                                    description = preset.description
                                    type = preset.type
                                    unit = preset.loadUnit
                                },
                                label = { Text(preset.name) }
                            )
                        }
                    }
                }
                AppTextField(name, { name = it }, "Название")
                AppTextField(muscle, { muscle = it }, "Группа мышц")
                AppTextField(description, { description = it }, "Описание", singleLine = false)
                Text("Тип упражнения")
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseType.entries.forEach {
                        FilterChip(selected = type == it, onClick = { type = it }, label = { Text(it.label()) })
                    }
                }
                Text("Единица нагрузки")
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoadUnit.entries.take(3).forEach {
                        FilterChip(selected = unit == it, onClick = { unit = it }, label = { Text(it.label()) })
                    }
                }
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoadUnit.entries.drop(3).forEach {
                        FilterChip(selected = unit == it, onClick = { unit = it }, label = { Text(it.label()) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, muscle, type, description, unit) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
