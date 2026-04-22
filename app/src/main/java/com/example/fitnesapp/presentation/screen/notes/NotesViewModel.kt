package com.example.fitnesapp.presentation.screen.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.Note
import com.example.fitnesapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val message: String? = null
)

class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeNotes().collect { notes -> _state.update { it.copy(notes = notes) } }
        }
    }

    fun save(id: Long?, title: String, text: String, workoutDate: Long?) {
        if (title.isBlank() || text.isBlank()) {
            _state.update { it.copy(message = "Заполните заголовок и текст") }
            return
        }
        viewModelScope.launch {
            repository.saveNote(Note(id = id ?: 0L, title = title.trim(), text = text.trim(), workoutDate = workoutDate))
            _state.update { it.copy(message = "Заметка сохранена") }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            _state.update { it.copy(message = "Заметка удалена") }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
