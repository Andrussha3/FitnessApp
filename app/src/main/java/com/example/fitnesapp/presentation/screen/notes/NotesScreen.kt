package com.example.fitnesapp.presentation.screen.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.example.fitnesapp.domain.model.Note
import com.example.fitnesapp.domain.model.toDateString
import com.example.fitnesapp.presentation.component.AppTextField
import com.example.fitnesapp.presentation.component.EmptyState
import com.example.fitnesapp.presentation.component.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: NotesViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Note?>(null) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои заметки") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { FloatingActionButton(onClick = { editing = Note(title = "", text = "") }) { Icon(Icons.Default.Add, null) } }
    ) { padding ->
        LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.notes.isEmpty()) {
                item { EmptyState("Нет заметок", "Добавьте заметку о самочувствии или тренировке") }
            }
            items(state.notes, key = { it.id }) { note ->
                SectionCard(title = note.title, subtitle = "Обновлено ${note.updatedAt.toDateString()}") {
                    Text(note.text)
                    TextButton(onClick = { editing = note }) { Text("Редактировать") }
                    TextButton(onClick = { viewModel.delete(note.id) }) { Text("Удалить") }
                }
            }
        }
    }
    editing?.let { current ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (current.id == 0L) "Новая заметка" else "Редактирование") },
            text = {
                var title by remember(current) { mutableStateOf(current.title) }
                var text by remember(current) { mutableStateOf(current.text) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(title, { title = it }, "Заголовок")
                    AppTextField(text, { text = it }, "Текст", singleLine = false)
                    TextButton(onClick = { viewModel.save(current.id.takeIf { it != 0L }, title, text, current.workoutDate); editing = null }) {
                        Text("Сохранить")
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Закрыть") } }
        )
    }
}
