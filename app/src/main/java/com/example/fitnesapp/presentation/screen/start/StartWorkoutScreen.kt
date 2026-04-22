package com.example.fitnesapp.presentation.screen.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.R
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.ImageBannerCard
import com.example.fitnesapp.presentation.component.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartWorkoutScreen(
    viewModel: StartWorkoutViewModel,
    onBack: () -> Unit,
    onOpenWorkout: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.navigateToSessionId) {
        state.navigateToSessionId?.let {
            onOpenWorkout(it)
            viewModel.consumeNavigation()
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Запуск тренировки") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.decision?.activeSessionId != null -> SectionCard(title = "Незавершенная тренировка", subtitle = state.decision?.message) {
                    Button(onClick = { viewModel.continueActive() }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { Text("Продолжить тренировку") }
                    TextButton(onClick = { viewModel.resetActiveWorkout() }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { Text("Сбросить тренировку") }
                    TextButton(onClick = onBack, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { Text("Назад") }
                }
                state.decision?.plannedDay == null -> SectionCard(title = "Сегодня", subtitle = state.decision?.message ?: "") {
                    EmptyState("Сегодня вы отдыхаете", "На сегодня тренировочный день не назначен", imageRes = R.drawable.empty_rest_day)
                    TextButton(onClick = onBack, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { Text("Назад") }
                }
                else -> SectionCard(title = "Готово к старту", subtitle = state.decision?.message ?: "Тренировка найдена") {
                    ImageBannerCard(
                        imageRes = R.drawable.theme_banner_sport,
                        title = state.decision?.plannedDay?.title ?: "Тренировка на сегодня",
                        subtitle = "Можно начать заново после сброса или при ручном запуске"
                    )
                    Button(onClick = { viewModel.startPlannedWorkout() }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { Text("Начать тренировку") }
                    TextButton(onClick = onBack, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { Text("Назад") }
                }
            }
        }
    }
}
