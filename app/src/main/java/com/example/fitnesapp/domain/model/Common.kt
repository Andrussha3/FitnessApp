package com.example.fitnesapp.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Gender {
    MALE,
    FEMALE,
    UNSPECIFIED
}

enum class ExerciseType {
    STRENGTH,
    CARDIO,
    STRETCH
}

enum class LoadUnit {
    KG,
    REPS,
    SECONDS,
    MINUTES,
    METERS
}

fun Gender.label(): String = when (this) {
    Gender.MALE -> "Мужчина"
    Gender.FEMALE -> "Женщина"
    Gender.UNSPECIFIED -> "Не указывать"
}

fun ExerciseType.label(): String = when (this) {
    ExerciseType.STRENGTH -> "Силовое"
    ExerciseType.CARDIO -> "Кардио"
    ExerciseType.STRETCH -> "Растяжка"
}

fun LoadUnit.label(): String = when (this) {
    LoadUnit.KG -> "кг"
    LoadUnit.REPS -> "повторения"
    LoadUnit.SECONDS -> "секунды"
    LoadUnit.MINUTES -> "минуты"
    LoadUnit.METERS -> "метры"
}

fun Int.dayLabel(): String = when (this) {
    DayOfWeek.MONDAY.value -> "Понедельник"
    DayOfWeek.TUESDAY.value -> "Вторник"
    DayOfWeek.WEDNESDAY.value -> "Среда"
    DayOfWeek.THURSDAY.value -> "Четверг"
    DayOfWeek.FRIDAY.value -> "Пятница"
    DayOfWeek.SATURDAY.value -> "Суббота"
    DayOfWeek.SUNDAY.value -> "Воскресенье"
    else -> "День"
}

fun Long.toDateString(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()
    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

fun Long.toShortDate(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(DateTimeFormatter.ofPattern("dd.MM"))

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
