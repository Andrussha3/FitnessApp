package com.example.fitnesapp.domain.model

enum class AppThemeMode {
    STANDARD,
    ORANGE_SPORT
}

fun AppThemeMode.label(): String = when (this) {
    AppThemeMode.STANDARD -> "Стандартная"
    AppThemeMode.ORANGE_SPORT -> "Оранжевая спортивная"
}
