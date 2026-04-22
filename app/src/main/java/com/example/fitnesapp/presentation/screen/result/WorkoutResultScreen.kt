package com.example.fitnesapp.presentation.screen.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.domain.model.PersonalRecordType
import com.example.fitnesapp.domain.model.toDateString
import com.example.fitnesapp.presentation.component.SectionCard

@Composable
fun WorkoutResultScreen(viewModel: WorkoutResultViewModel, onFinish: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session = state.session
    Scaffold { padding ->
        Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Молодец!", subtitle = session?.completedAt?.toDateString()) {
                Text("Тренировочный день: ${session?.dayLabel.orEmpty()}")
                Text("Упражнений выполнено: ${session?.exercises?.count { !it.skipped } ?: 0}")
                Text(state.message)
            }
            if (state.records.isNotEmpty()) {
                SectionCard(title = "Новые личные рекорды") {
                    state.records.forEach { Text("${it.exerciseName}: ${recordLabel(it.recordType)}: ${formatRecordValue(it.value)}") }
                }
            }
            if (state.recommendations.isNotEmpty()) {
                SectionCard(title = "Рекомендации по следующей тренировке") {
                    state.recommendations.forEach { Text(it.message) }
                }
            }
            Button(onClick = onFinish, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                Text("На главный экран")
            }
        }
    }
}

private fun recordLabel(type: PersonalRecordType): String = when (type) {
    PersonalRecordType.MAX_WEIGHT -> "Максимальный вес"
    PersonalRecordType.MAX_REPS -> "Максимум повторений"
    PersonalRecordType.MAX_EXERCISE_VOLUME -> "Объем упражнения"
    PersonalRecordType.MAX_WORKOUT_VOLUME -> "Объем тренировки"
}

private fun formatRecordValue(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
