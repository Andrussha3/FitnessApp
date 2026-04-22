package com.example.fitnesapp.data.repository

import android.content.Context
import com.example.fitnesapp.domain.model.AppThemeMode
import com.example.fitnesapp.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepositoryImpl(context: Context) : ThemeRepository {
    private val prefs = context.getSharedPreferences("fitnes_theme", Context.MODE_PRIVATE)
    private val key = "app_theme_mode"
    private val state = MutableStateFlow(readTheme())

    override fun observeTheme(): Flow<AppThemeMode> = state.asStateFlow()

    override suspend fun setTheme(theme: AppThemeMode) {
        prefs.edit().putString(key, theme.name).apply()
        state.value = theme
    }

    private fun readTheme(): AppThemeMode = prefs.getString(key, AppThemeMode.STANDARD.name)
        ?.let {
            runCatching { AppThemeMode.valueOf(it) }.getOrDefault(AppThemeMode.STANDARD)
        }
        ?: AppThemeMode.STANDARD
}
