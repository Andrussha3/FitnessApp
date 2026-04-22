package com.example.fitnesapp.presentation.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.domain.model.PersonalRecordType
import com.example.fitnesapp.domain.model.toDateString
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryDetailsScreen(viewModel: WorkoutHistoryDetailsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val details = state.details
    Scaffold(
        topBar = { TopAppBar(title = { Text("Детали тренировки") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        if (details == null) {
            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding)) {
                EmptyState("Тренировка не найдена", "Не удалось загрузить детали истории")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(title = details.session.dayLabel, subtitle = details.session.completedAt?.toDateString()) {
                    Text("Длительность: ${details.durationMinutes} мин")
                    Text("Общий объем: ${details.totalVolume.toInt()}")
                    Text(details.comparisonMessage)
                }
            }
            items(details.session.exercises, key = { it.id }) { exercise ->
                SectionCard(title = exercise.exerciseName, subtitle = "Подходов: ${exercise.sets.size}") {
                    exercise.sets.forEach { set ->
                        Text("Подход ${set.orderInExercise}: вес ${set.actualWeight ?: set.targetWeight ?: 0.0}, повт ${set.actualReps ?: set.targetReps ?: 0}, статус ${if (set.skipped) "пропущен" else if (set.completed) "выполнен" else "не завершен"}")
                    }
                }
            }
            if (details.recommendations.isNotEmpty()) {
                item {
                    SectionCard(title = "Рекомендации по прогрессии") {
                        details.recommendations.forEach { Text(it.message) }
                    }
                }
            }
            if (details.personalRecords.isNotEmpty()) {
                item {
                    SectionCard(title = "Личные рекорды") {
                        details.personalRecords.forEach {
                            Text("${it.exerciseName}: ${recordTypeLabel(it.recordType)} ${it.value}")
                        }
                    }
                }
            }
        }
    }
}

private fun recordTypeLabel(type: PersonalRecordType): String = when (type) {
    PersonalRecordType.MAX_WEIGHT -> "макс. вес"
    PersonalRecordType.MAX_REPS -> "макс. повторения"
    PersonalRecordType.MAX_EXERCISE_VOLUME -> "макс. объем упражнения"
    PersonalRecordType.MAX_WORKOUT_VOLUME -> "макс. объем тренировки"
}
