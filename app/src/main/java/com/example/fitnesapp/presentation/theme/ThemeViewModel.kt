package com.example.fitnesapp.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesapp.domain.model.AppThemeMode
import com.example.fitnesapp.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {
    private val _theme = MutableStateFlow(AppThemeMode.STANDARD)
    val theme: StateFlow<AppThemeMode> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.observeTheme().collect { _theme.value = it }
        }
    }
}
