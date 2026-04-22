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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.domain.model.toDateString
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(viewModel: WorkoutHistoryViewModel, onBack: () -> Unit, onOpenDetails: (Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("История тренировок") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.items.isEmpty()) {
                item { EmptyState("История пуста", "После первой завершенной тренировки здесь появятся записи") }
            }
            items(state.items, key = { it.sessionId }) { item ->
                SectionCard(title = item.title, subtitle = item.completedAt.toDateString()) {
                    Text("Упражнений: ${item.exerciseCount}")
                    Text("Выполненных подходов: ${item.completedSetsCount}")
                    Text("Объем: ${item.totalVolume.toInt()}")
                    Text("Длительность: ${item.durationMinutes} мин")
                    Text(item.comparisonLabel)
                    TextButton(onClick = { onOpenDetails(item.sessionId) }) { Text("Открыть детали") }
                }
            }
        }
    }
}
