package com.example.fitnesapp.presentation.navigation

object Destinations {
    const val Home = "home"
    const val Exercises = "exercises"
    const val Workouts = "workouts"
    const val Progress = "progress"
    const val History = "history"
    const val HistoryDetails = "history_details/{sessionId}"
    const val Notes = "notes"
    const val Settings = "settings"
    const val StartWorkout = "start_workout"
    const val ActiveWorkout = "active_workout/{sessionId}"
    const val Result = "result/{sessionId}"

    fun activeWorkout(sessionId: Long) = "active_workout/$sessionId"
    fun result(sessionId: Long) = "result/$sessionId"
    fun historyDetails(sessionId: Long) = "history_details/$sessionId"
}
