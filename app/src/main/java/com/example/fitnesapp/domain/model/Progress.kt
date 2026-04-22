package com.example.fitnesapp.domain.model

data class ProgressPoint(
    val timestamp: Long,
    val label: String,
    val weight: Double,
    val reps: Int,
    val volume: Double
)

data class ProgressSummary(
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val bestVolume: Double = 0.0,
    val averageVolume: Double = 0.0
)

data class ProgressReport(
    val points: List<ProgressPoint> = emptyList(),
    val history: List<WorkoutSession> = emptyList(),
    val summary: ProgressSummary = ProgressSummary()
)
