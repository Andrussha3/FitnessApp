package com.example.fitnesapp.presentation.screen.progress

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.R
import com.example.fitnesapp.domain.model.label
import com.example.fitnesapp.domain.model.toDateString
import com.example.fitnesapp.presentation.component.ImageBannerCard
import com.example.fitnesapp.presentation.component.SectionCard
import com.example.fitnesapp.presentation.component.SimpleLineChart
import com.example.fitnesapp.presentation.component.StatTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Мой прогресс") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ImageBannerCard(
                    imageRes = R.drawable.banner_progress_fire,
                    title = "Прогресс в темном техно-стиле",
                    subtitle = "Локальная аналитика, объем и динамика без сервера"
                )
            }
            item {
                SectionCard(title = "Фильтры") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = androidx.compose.ui.Modifier.horizontalScroll(rememberScrollState())) {
                        TextButton(onClick = { viewModel.selectExercise(null) }) { Text("Все упражнения") }
                        state.exercises.forEach { exercise ->
                            TextButton(onClick = { viewModel.selectExercise(exercise.id) }) { Text(exercise.name) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 30, 90, null).forEach { period ->
                            TextButton(onClick = { viewModel.selectPeriod(period) }) { Text(period?.toString() ?: "Все") }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Тренировок", state.report.summary.totalWorkouts.toString())
                    StatTile("Объем", "${state.report.summary.totalVolume.toInt()}")
                    StatTile("Средний", "${state.report.summary.averageVolume.toInt()}")
                }
            }
            if (state.goalMetrics.isNotEmpty()) {
                item {
                    SectionCard(title = "Акцент по цели") {
                        state.goalMetrics.forEach {
                            Text("${it.title}: ${it.primaryValue}")
                            Text(it.subtitle)
                            Text("Вторичный показатель: ${it.secondaryValue}")
                        }
                    }
                }
            }
            if (state.muscleStats.isNotEmpty()) {
                item {
                    SectionCard(title = "По мышечным группам") {
                        state.muscleStats.sortedByDescending { it.totalVolume }.forEach {
                            Text("${it.group.label()} • подходов ${it.completedSets} • объем ${it.totalVolume.toInt()} • доля ${it.sharePercent.toInt()}%${if (it.isLagging) " • отстает" else ""}")
                        }
                    }
                }
            }
            item {
                SectionCard(title = "График рабочего веса") {
                    SimpleLineChart(state.report.points.map { it.weight.toFloat() }, state.report.points.map { it.label })
                }
            }
            item {
                SectionCard(title = "График повторений") {
                    SimpleLineChart(state.report.points.map { it.reps.toFloat() }, state.report.points.map { it.label })
                }
            }
            item {
                SectionCard(title = "График объема") {
                    SimpleLineChart(state.report.points.map { it.volume.toFloat() }, state.report.points.map { it.label })
                }
            }
            items(state.report.history, key = { it.id }) { session ->
                SectionCard(title = session.dayLabel, subtitle = session.startedAt.toDateString()) {
                    Text("Упражнений: ${session.exercises.size}")
                    Text("Силовой объем: ${session.exercises.flatMap { it.sets }.sumOf { ((it.actualWeight ?: 0.0) * (it.actualReps ?: 0)).toInt() }}")
                }
            }
        }
    }
}
