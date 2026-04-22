package com.example.fitnesapp.presentation.screen.settings

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.fitnesapp.R
import com.example.fitnesapp.domain.model.AppThemeMode
import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.Gender
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.toDateString
import com.example.fitnesapp.domain.model.label
import com.example.fitnesapp.presentation.component.AppTextField
import com.example.fitnesapp.presentation.component.ImageBannerCard
import com.example.fitnesapp.presentation.component.SectionCard
import com.example.fitnesapp.presentation.component.ThemeOptionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingMeasurement by remember { mutableStateOf<BodyMeasurement?>(null) }
    var confirmClearProgress by remember { mutableStateOf(false) }
    var confirmClearHistory by remember { mutableStateOf(false) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImageBannerCard(
                imageRes = R.drawable.banner_profile_digital,
                title = "Премиум sports-tech",
                subtitle = "Настройте внешний вид и профиль под свой тренировочный ритм"
            )
            SectionCard(title = "Профиль") {
                AppTextField(state.name, viewModel::onNameChanged, "Имя")
                AppTextField(state.age, viewModel::onAgeChanged, "Возраст")
                AppTextField(state.height, viewModel::onHeightChanged, "Рост, см")
                AppTextField(state.weight, viewModel::onWeightChanged, "Вес, кг")
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Gender.entries.forEach {
                        TextButton(onClick = { viewModel.onGenderChanged(it) }) { Text(if (state.gender == it) "${it.label()} ✓" else it.label()) }
                    }
                }
                Button(onClick = { viewModel.saveProfile() }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    Text("Сохранить профиль")
                }
            }
            SectionCard(title = "Цели пользователя") {
                UserGoal.entries.forEach {
                    TextButton(onClick = { viewModel.onGoalChanged(it) }) { Text(if (state.goal == it) "${it.label()} ✓" else it.label()) }
                }
                AppTextField(state.goalNote, viewModel::onGoalNoteChanged, "Комментарий к цели", singleLine = false)
            }
            SectionCard(title = "Измерения тела", subtitle = "История замеров сохраняется локально") {
                Button(onClick = { editingMeasurement = BodyMeasurement(measuredAt = System.currentTimeMillis()) }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    Text("Добавить замер")
                }
                state.measurements.take(5).forEach { item ->
                    Text("${item.measuredAt.toDateString()} • вес ${item.bodyWeightKg ?: 0.0} кг • талия ${item.waistCm ?: 0.0} см")
                    TextButton(onClick = { editingMeasurement = item }) { Text("Редактировать") }
                    TextButton(onClick = { viewModel.deleteMeasurement(item.id) }) { Text("Удалить") }
                }
            }
            SectionCard(title = "Переключить тему", subtitle = "Выбор сохраняется локально и восстанавливается после перезапуска") {
                ThemeOptionCard(
                    title = "Стандартная",
                    selected = state.selectedTheme == AppThemeMode.STANDARD,
                    imageRes = R.drawable.banner_profile_digital,
                    onClick = { viewModel.setTheme(AppThemeMode.STANDARD) }
                )
                ThemeOptionCard(
                    title = "Оранжевая спортивная",
                    selected = state.selectedTheme == AppThemeMode.ORANGE_SPORT,
                    imageRes = R.drawable.theme_banner_sport,
                    onClick = { viewModel.setTheme(AppThemeMode.ORANGE_SPORT) }
                )
            }
            SectionCard(title = "Управление данными") {
                Button(onClick = { confirmClearProgress = true }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    Text("Сбросить мой прогресс")
                }
                Text("Очистит данные прогресса, личные рекорды, рекомендации и замеры тела.")
                Button(onClick = { confirmClearHistory = true }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    Text("Сбросить историю тренировок")
                }
                Text("Очистит сохраненную историю тренировок и связанные записи аналитики.")
            }
        }
    }
    editingMeasurement?.let { item ->
        MeasurementEditorDialog(item, onDismiss = { editingMeasurement = null }, onSave = {
            viewModel.saveMeasurement(it)
            editingMeasurement = null
        })
    }
    if (confirmClearProgress) {
        AlertDialog(
            onDismissRequest = { confirmClearProgress = false },
            title = { Text("Сбросить мой прогресс?") },
            text = { Text("Будут очищены данные прогресса, графики, рекорды, рекомендации и история замеров тела.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearProgressData()
                    confirmClearProgress = false
                }) { Text("Очистить") }
            },
            dismissButton = { TextButton(onClick = { confirmClearProgress = false }) { Text("Отмена") } }
        )
    }
    if (confirmClearHistory) {
        AlertDialog(
            onDismissRequest = { confirmClearHistory = false },
            title = { Text("Сбросить историю тренировок?") },
            text = { Text("Будут удалены завершенные тренировки и связанные аналитические записи истории.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearWorkoutHistory()
                    confirmClearHistory = false
                }) { Text("Очистить") }
            },
            dismissButton = { TextButton(onClick = { confirmClearHistory = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun MeasurementEditorDialog(initial: BodyMeasurement, onDismiss: () -> Unit, onSave: (BodyMeasurement) -> Unit) {
    var weight by remember(initial) { mutableStateOf(initial.bodyWeightKg?.toString().orEmpty()) }
    var chest by remember(initial) { mutableStateOf(initial.chestCm?.toString().orEmpty()) }
    var waist by remember(initial) { mutableStateOf(initial.waistCm?.toString().orEmpty()) }
    var belly by remember(initial) { mutableStateOf(initial.bellyCm?.toString().orEmpty()) }
    var hips by remember(initial) { mutableStateOf(initial.hipsCm?.toString().orEmpty()) }
    var biceps by remember(initial) { mutableStateOf(initial.bicepsCm?.toString().orEmpty()) }
    var thigh by remember(initial) { mutableStateOf(initial.thighCm?.toString().orEmpty()) }
    var calf by remember(initial) { mutableStateOf(initial.calfCm?.toString().orEmpty()) }
    var note by remember(initial) { mutableStateOf(initial.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Замеры тела") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = androidx.compose.ui.Modifier.verticalScroll(rememberScrollState())) {
                AppTextField(weight, { weight = it }, "Вес тела, кг")
                AppTextField(chest, { chest = it }, "Грудь, см")
                AppTextField(waist, { waist = it }, "Талия, см")
                AppTextField(belly, { belly = it }, "Живот, см")
                AppTextField(hips, { hips = it }, "Бедра, см")
                AppTextField(biceps, { biceps = it }, "Бицепс, см")
                AppTextField(thigh, { thigh = it }, "Бедро ноги, см")
                AppTextField(calf, { calf = it }, "Икра, см")
                AppTextField(note, { note = it }, "Заметка", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(initial.copy(
                    bodyWeightKg = weight.replace(',', '.').toDoubleOrNull(),
                    chestCm = chest.replace(',', '.').toDoubleOrNull(),
                    waistCm = waist.replace(',', '.').toDoubleOrNull(),
                    bellyCm = belly.replace(',', '.').toDoubleOrNull(),
                    hipsCm = hips.replace(',', '.').toDoubleOrNull(),
                    bicepsCm = biceps.replace(',', '.').toDoubleOrNull(),
                    thighCm = thigh.replace(',', '.').toDoubleOrNull(),
                    calfCm = calf.replace(',', '.').toDoubleOrNull(),
                    note = note
                ))
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
